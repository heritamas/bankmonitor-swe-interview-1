package bankmonitor.service;

import bankmonitor.error.DTOError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.Transaction;
import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionData;
import bankmonitor.model.TransactionV2;
import bankmonitor.repository.LegacyTransactionRepository;
import bankmonitor.repository.TransactionDataRepository;
import bankmonitor.repository.TransactionV2Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionV2Repository transactionV2Repository;

    private final LegacyTransactionRepository legacyTransactionRepository;

    private final Conversions conversions;

    @Autowired
    public TransactionService(TransactionV2Repository transactionV2Repository, LegacyTransactionRepository legacyTransactionRepository, Conversions conversions) {
        this.transactionV2Repository = transactionV2Repository;
        this.legacyTransactionRepository = legacyTransactionRepository;
        this.conversions = conversions;
    }

    /**
     * Anything read from the DB might have been modified by legacy code. It is this service's responsibility to
     * sync the fields of the DTO with the fields of the entity.
     */
    private Either<TransactionError, TransactionV2> syncFields(TransactionV2 tr2) {
        return Try.of(() -> {
            TransactionData fromJson = conversions.fromJson(tr2.getId(), tr2.getData());
            TransactionData fromTransaction = tr2.getTransactionData();
            if (!fromJson.equals(fromTransaction)) {
                logger.warn("Transaction data and JSON are out of sync for transaction {} : {} != {}", tr2.getId(), tr2.getData(), tr2.getTransactionData());
                tr2.setTransactionData(fromJson);
                transactionV2Repository.save(tr2);
            }
            return tr2;
        })
                .toEither()
                .mapLeft(exc -> {
                    logger.warn("Error while syncing fields: {}", exc.getMessage());
                    return new DTOError.InvalidJSON(exc.getMessage());
                });
    }

    private Optional<TransactionV2> fetchById(Transaction tr) {
        return transactionV2Repository.findById(tr.getId());
    }

    public List<Either<TransactionError, TransactionV2>> getAllTransactions() {
        var joined = transactionV2Repository.findAll();

        return joined
                .stream()
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
        return fetchById(tr)
                .map(this::syncFields)
                .orElse(Either.left(new DTOError.NotFound(tr.getId())));
    }

    /**
     * Update an existing transaction described by transaction.
     * @param tr2
     * @return structure describing the result of the operation
     */
    public Either<TransactionError, TransactionV2> updateTransaction(TransactionV2 tr2) {
        return Try.of(() -> {
            // sync data field, tr2 is the source of truth
            tr2.setData(conversions.toJSON(tr2.getTransactionData().getDetails()));
            transactionV2Repository.save(tr2);
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
        var joined = transactionV2Repository.findById(id);
        return joined
                .map(this::syncFields)
                .orElseGet(() -> Either.left(new DTOError.NotFound(id)));
    }
}

