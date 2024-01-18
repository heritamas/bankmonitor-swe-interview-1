package bankmonitor.controller;

import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionUpdateDTO;
import bankmonitor.service.Conversions;
import bankmonitor.service.TransactionService;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v2/transactions")
public class TransactionControllerV2 {
    private final TransactionService transactionService;

    private final Conversions conversions;

    @Autowired
    public TransactionControllerV2(TransactionService transactionService, Conversions conversions) {
        this.transactionService = transactionService;
        this.conversions = conversions;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService
                        .getAllTransactions()
                        .stream()
                        .flatMap(tr -> Try.of(() -> conversions.toDTO(tr)).toJavaStream())
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
            return null;
            /*
            return transactionService.findTransactionById(id)
                    .map(transaction -> {
                        transaction.getData().setAmount(updateDTO.getAmount());
                        transaction.getData().setReference(updateDTO.getReference());
                        return ResponseEntity.ok(transactionService.saveTransaction(transaction));
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());

             */
        }
    }

}
