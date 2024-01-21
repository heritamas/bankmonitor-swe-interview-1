package bankmonitor.service;

import bankmonitor.error.DTOError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.*;
import bankmonitor.repository.LegacyTransactionRepository;
import bankmonitor.repository.TransactionV2Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private Either<TransactionError, TransactionV2> syncFieldsFromJSON(TransactionV2 tr2) {
        return Try.of(() -> {
            TransactionData fromJson = conversions.fromJson(tr2.getId(), tr2.getData());
            TransactionData fromFields = tr2.getTransactionData();
            if (!fromJson.equals(fromFields)) {
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

    private Either<TransactionError, TransactionV2> syncJSONFromFields(TransactionV2 tr2) {
        return Try.of(() -> {
                    String fromFields = conversions.toJSON(tr2.getTransactionData().getDetails());
                    String fromJson = tr2.getData();
                    if (!fromFields.equals(fromJson)) {
                        logger.warn("Transaction data and JSON are out of sync for transaction : {} != {}", tr2.getData(), tr2.getTransactionData());
                        tr2.setData(fromFields);
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

    /**
     * Anything read from the DB might have been modified by legacy code. It is this service's responsibility to
     * sync the fields. In this case we sync from data (JSON) to transactionData (entity).
     */
    public List<Either<TransactionError, TransactionV2>> getAllTransactions() {
        var joined = transactionV2Repository.findAll();

        return joined
                .stream()
                .map(this::syncFieldsFromJSON)
                .toList();
    }

    /**
     * Create a new transaction from a DTO (basically JSON string data)
     * Anything created is new, and happens on the V2 API, so the sync is from transactionData (entity) to data (JSON).
     * @param createDTO : transaction to be created
     * @return structure describing the result of the operation
     */
    public Either<TransactionError, TransactionV2> saveTransaction(TransactionDataDTO createDTO) {
        /*
        return Try.of(() -> {
            var json = conversions.toJSON(createDTO);
            var tr = legacyTransactionRepository.save(new Transaction(json));
            return TransactionV2.builder()
                    .id(tr.getId())
                    .timestamp(tr.getTimestamp())
                    .data(json)
                    .transactionData(TransactionData.builder()
                            .id(tr.getId())
                            .details(createDTO)
                            .build())
                    .build();
            })
                .toEither()
                .flatMap(this::syncJSONFromFields)
                .mapLeft(exc -> {
                    logger.warn("Error while saving transaction: {}", exc.getMessage());
                    // or something went wrong during the save
                    return new DTOError.SaveError(exc.getMessage());
                });
         */
        var transaction = TransactionV2.builder()
                .timestamp(LocalDateTime.now())
                .transactionData(TransactionData.builder()
                        .details(createDTO)
                        .build())
                .build();

        return syncJSONFromFields(transaction);
    }

    /**
     * Update an existing transaction described by transaction.
     * @param tr2 : DTO describing the transaction
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
        System.out.println("joined = " + joined);
        return joined
                .map(this::syncFieldsFromJSON)
                .orElseGet(() -> Either.left(new DTOError.NotFound(id)));
    }
}

