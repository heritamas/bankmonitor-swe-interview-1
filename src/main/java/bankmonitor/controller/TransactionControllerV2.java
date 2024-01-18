package bankmonitor.controller;

import bankmonitor.model.TransactionUpdateDTO;
import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionV2;
import bankmonitor.service.TransactionService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v2/transactions")
@RequiredArgsConstructor
public class TransactionControllerV2 {
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionV2>> getAllTransactions() {
        return ResponseEntity.ok(transactionService
                        .getAllTransactions()
                        .stream()
                        .map(Either::get)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody @Valid TransactionDTO transaction, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(transactionService.saveTransaction(transaction));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestBody @Valid TransactionUpdateDTO updateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        } else {
            return transactionService.findTransactionById(id)
                    .map(transaction -> {
                        transaction.getData().setAmount(updateDTO.getAmount());
                        transaction.getData().setReference(updateDTO.getReference());
                        return ResponseEntity.ok(transactionService.saveTransaction(transaction));
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
    }

}
