package bankmonitor.controller;

import bankmonitor.error.DTOError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionUpdateDTO;
import bankmonitor.service.Conversions;
import bankmonitor.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v2/transactions")
public class TransactionControllerV2 {

    private static final Logger logger = LoggerFactory.getLogger(TransactionControllerV2.class);
    private final TransactionService transactionService;

    private final Conversions conversions;

    @Autowired
    public TransactionControllerV2(TransactionService transactionService, Conversions conversions) {
        this.transactionService = transactionService;
        this.conversions = conversions;
    }
    record ErrorResponse(Integer code, String message) {}

    static String toJSON(ErrorResponse err) {
        return Try
            .of(() -> new ObjectMapper().writeValueAsString(err))
            .recover(exc -> {
                logger.error("Error while serializing error response: {}", exc.getMessage());
                return "Unidentified error.";
            }).get();
    }

    static ResponseEntity<String> mapError(TransactionError err) {
        ErrorResponse response = new ErrorResponse(500, "Internal server error");
        if (err instanceof DTOError.NotFound notfound) {
            response = new ErrorResponse(err.errorCode(), "Transaction with id " + notfound.id() + " not found");
        } else if (err instanceof DTOError.InvalidJSON invalid) {
            response = new ErrorResponse(err.errorCode(), invalid.message());
        } else if (err instanceof DTOError.UpdateError update) {
            response = new ErrorResponse(err.errorCode(), update.message());
        }

        return ResponseEntity
                .status(response.code())
                .body(toJSON(response));
    }
    static ResponseEntity<String> mapResult(TransactionDTO dto) {
        return ResponseEntity
                .ok()
                .body(dto.getData());
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService
                        .getAllTransactions()
                        .stream()
                        .map(either -> either.flatMap(conversions.lift(conversions::toDTO, exc -> new DTOError.InvalidJSON(exc.getMessage()))))
                        .filter(Either::isRight)
                        .map(Either::get)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<String> createTransaction(@RequestBody @Valid TransactionDTO transaction, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        } else {
            var saved = transactionService.saveTransaction(transaction);
            return saved
                    .flatMap(conversions.lift(conversions::toDTO, exc -> new DTOError.InvalidJSON(exc.getMessage())))
                    .fold(
                            TransactionControllerV2::mapError,
                            TransactionControllerV2::mapResult
                    );

        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTransaction(@PathVariable Long id, @RequestBody @Valid TransactionUpdateDTO updateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        } else {
            return transactionService.findTransactionById(id)
                    .flatMap(tr2 -> {
                                tr2.getTransactionData().getDetails().setAmount(updateDTO.getAmount());
                                tr2.getTransactionData().getDetails().setReference(updateDTO.getReference());
                                return transactionService
                                        .updateTransaction(tr2)
                                        .flatMap(conversions.lift(conversions::toDTO, exc -> new DTOError.InvalidJSON(exc.getMessage())));
                            }
                    )
                    .fold(
                            TransactionControllerV2::mapError,
                            TransactionControllerV2::mapResult
                    );
        }
    }

}
