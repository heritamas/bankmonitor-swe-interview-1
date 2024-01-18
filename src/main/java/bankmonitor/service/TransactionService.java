package bankmonitor.service;

import bankmonitor.error.DTOError;
import bankmonitor.error.GeneralError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionV2;
import bankmonitor.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    private static Either<TransactionError, TransactionV2> convertToTransactionV2(TransactionDTO transactionDTO) {
        return Try.of(() -> TransactionV2.of(transactionDTO))
                .toEither()
                .mapLeft(exc -> {
                    if (exc instanceof JsonProcessingException) {
                        logger.warn("Invalid JSON: {}", exc.getMessage());
                        return new DTOError.InvalidJSON(exc.getMessage());
                    } else {
                        return new GeneralError.General("Unknown error");
                    }
                });
    }


    public List<Either<TransactionError, TransactionV2>> getAllTransactions() {
        return transactionRepository
                .findAll()
                .stream()
                .map(TransactionService::convertToTransactionV2)
                .toList();
    }

    public TransactionDTO createTransaction(String jsonData) {
        return saveTransaction(new TransactionDTO(jsonData));
    }

    public TransactionDTO saveTransaction(TransactionDTO transaction) {
        return transactionRepository.save(transaction);
    }

    public Either<TransactionError, TransactionV2> findTransactionById(Long id) {

        return transactionRepository
                .findById(id)
                .map(TransactionService::convertToTransactionV2)
                .orElseGet(() -> Either.left(new DTOError.NotFound(id)));

    }
}

