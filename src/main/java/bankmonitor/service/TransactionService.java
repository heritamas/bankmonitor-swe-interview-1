package bankmonitor.service;

import bankmonitor.error.DTOError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.Transaction;
import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionData;
import bankmonitor.model.TransactionV2;
import bankmonitor.repository.TransactionDataRepository;
import bankmonitor.repository.LegacyTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionDataRepository transactionDataRepository;

    private final LegacyTransactionRepository legacyTransactionRepository;

    private final Conversions conversions;

    @Autowired
    public TransactionService(TransactionDataRepository transactionDataRepository, LegacyTransactionRepository legacyTransactionRepository, Conversions conversions) {
        this.transactionDataRepository = transactionDataRepository;
        this.legacyTransactionRepository = legacyTransactionRepository;
        this.conversions = conversions;
    }

    /**
     * Anything read from the DB might have been modified by legacy code. It is this service's responsibility to
     * sync the fields of the DTO with the fields of the entity.
     */
    private Either<TransactionError, TransactionV2> syncFields(TransactionV2 tr) {
        return Try.of(() -> {
            TransactionData fromJson = conversions.fromJson(tr.getId(), tr.getData());
            TransactionData fromTransaction = tr.getTransactionData();
            if (!fromJson.equals(fromTransaction)) {
                logger.warn("Transaction data and JSON are out of sync for transaction {} : {} != {}", tr.getId(), tr.getData(), tr.getTransactionData());
                tr.setTransactionData(fromJson);
                transactionDataRepository.save(fromJson);
            }
            return tr;
        })
                .toEither()
                .mapLeft(exc -> {
                    logger.warn("Error while syncing fields: {}", exc.getMessage());
                    return new DTOError.InvalidJSON(exc.getMessage());
                });
    }

    private TransactionV2 fetchById(Transaction tr) {
        var trd = transactionDataRepository.findById(tr.getId());
        logger.info("Fetched transaction data: {}", trd);
        return TransactionV2.builder()
                .id(tr.getId())
                .timestamp(tr.getTimestamp())
                .data(tr.getData())
                .transactionData(trd.orElse(null))
                .build();
    }


    public List<Either<TransactionError, TransactionV2>> getAllTransactions() {
        var legacyTransactions = legacyTransactionRepository.findAll();

        return legacyTransactions
                .stream()
                .map(this::fetchById)
                .map(this::syncFields)
                .toList();
    }

    /**
     * Create a new transaction from a DTO (basically JSON string data)
     * @param dto
     * @return structure describing the result of the operation
     */
    public Either<TransactionError, TransactionV2> saveTransaction(TransactionDTO dto) {
        // legacy part
        var tr = legacyTransactionRepository.save(new Transaction(dto.getData()));
        // transactionData part
        return syncFields(fetchById(tr));
    }

    /**
     * Update an existing transaction described by transaction.
     * @param tr2
     * @return structure describing the result of the operation
     */
    public Either<TransactionError, TransactionV2> updateTransaction(TransactionV2 tr2) {
        return Try.of(() -> {
            // sync data field, tr2 is the source of truth
            var json = conversions.toJSON(tr2.getTransactionData().getDetails());
            tr2.setData(json);
            var transaction = Transaction.builder()
                    .id(tr2.getId())
                    .timestamp(tr2.getTimestamp())
                    .data(json)
                    .build();
            legacyTransactionRepository.save(transaction);
            transactionDataRepository.save(tr2.getTransactionData());
            return tr2;
        })
                .toEither()
                .mapLeft(exc -> {
                    logger.warn("Error while updating transaction: {}", exc.getMessage());
                    if (exc instanceof JsonProcessingException) {
                        // either we could not deserialize into JSON
                        return new DTOError.InvalidJSON(exc.getMessage());
                    } else {
                        // or something else went wrong during the update
                        return new DTOError.UpdateError(exc.getMessage());
                    }
                });
    }


    public Either<TransactionError, TransactionV2> findTransactionById(Long id) {
        var legacyTransaction = legacyTransactionRepository.findById(id);
        return legacyTransaction
                .map(this::fetchById)
                .map(this::syncFields)
                .orElseGet(() -> Either.left(new DTOError.NotFound(id)));
    }
}

