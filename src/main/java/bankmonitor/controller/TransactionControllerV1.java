package bankmonitor.controller;

import bankmonitor.model.Transaction;
import bankmonitor.repository.LegacyTransactionRepository;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionControllerV1 {

    private static final Logger logger = LoggerFactory.getLogger(TransactionControllerV1.class);
    private final LegacyTransactionRepository transactionRepository;

    @Autowired
    public TransactionControllerV1(LegacyTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    @ResponseBody
    public List<Transaction> getAllTransactions() {
        var result = transactionRepository.findAll();
        logger.debug("Getting all transactions: {}", result);

        return result;
    }

    @PostMapping
    @ResponseBody
    public Transaction createTransaction(@RequestBody String jsonData) {
        Transaction data = new Transaction(jsonData);
        return transactionRepository.save(data);
    }

    @PutMapping("/{id}")
    @ResponseBody
    @SneakyThrows
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody String update) {

        JSONObject updateJson = new JSONObject(update);

        Optional<Transaction> data = transactionRepository.findById(id);
        if (!data.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = data.get();
        JSONObject trdata = new JSONObject(transaction.getData());

        if (updateJson.has(Transaction.AMOUNT_KEY)) {
            trdata.put(Transaction.AMOUNT_KEY, updateJson.getInt(Transaction.AMOUNT_KEY));
        }

        if (updateJson.has(Transaction.REFERENCE_KEY)) {
            trdata.put(Transaction.REFERENCE_KEY, updateJson.getString(Transaction.REFERENCE_KEY));
        }
        transaction.setData(trdata.toString());

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }
}
