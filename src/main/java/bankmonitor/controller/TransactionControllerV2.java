package bankmonitor.controller;

import bankmonitor.error.ApiErrorException;
import bankmonitor.error.DTOError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.*;
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
import java.time.LocalDateTime;
import java.util.List;

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

    static ApiErrorException mapError(TransactionError err) {
        ApiErrorException response = new ApiErrorException(500, "Internal server error");
        if (err instanceof DTOError.NotFound notfound) {
            response = new ApiErrorException(err.errorCode(), "Transaction with id " + notfound.id() + " not found");
        } else if (err instanceof DTOError.InvalidJSON invalid) {
            response = new ApiErrorException(err.errorCode(), invalid.message());
        } else if (err instanceof DTOError.UpdateError update) {
            response = new ApiErrorException(err.errorCode(), update.message());
        } else if (err instanceof DTOError.SaveError create) {
            response = new ApiErrorException(err.errorCode(), create.message());
        }

        return response;
    }
    static ResponseEntity<TransactionDTO> mapResult(TransactionDTO dto) {
        return ResponseEntity
                .ok()
                .body(dto);
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

    @PostMapping(produces = "application/json")
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody @Valid TransactionDataDTO createDTO, BindingResult bindingResult) throws ApiErrorException {
        if (bindingResult.hasErrors()) {
            logger.error("Error while validating create: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().build();
        } else {
            var saved = transactionService.saveTransaction(createDTO);
            return saved
                    .flatMap(conversions.lift(conversions::toDTO, exc -> new DTOError.InvalidJSON(exc.getMessage())))
                    .map(TransactionControllerV2::mapResult)
                    .getOrElseThrow(TransactionControllerV2::mapError);
        }
    }

    @PutMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestBody @Valid TransactionUpdateDTO updateDTO, BindingResult bindingResult) throws ApiErrorException {
        if (bindingResult.hasErrors()) {
            logger.error("Error while validating update: {}", bindingResult.getAllErrors());
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
                    .map(TransactionControllerV2::mapResult)
                    .getOrElseThrow(TransactionControllerV2::mapError);
        }
    }

}
