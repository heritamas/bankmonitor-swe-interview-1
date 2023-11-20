package bankmonitor.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.json.JSONObject;

import bankmonitor.model.Transaction;
import bankmonitor.repository.TransactionRepository;

@Controller
@RequestMapping("/")
public class TransactionController {

	@Autowired
	private TransactionRepository transactionRepository;

	@GetMapping("/transactions")
	@ResponseBody
	public List<Transaction> getAllTransactions() {
		return transactionRepository.findAll();
	}

	@PostMapping("/transactions")
	@ResponseBody
	public Transaction createTransaction(@RequestBody String jsonData) {
    Transaction data = new Transaction(jsonData);
		return transactionRepository.save(data);
	}

	@PutMapping("/transactions/{id}")
	@ResponseBody
	public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody String update) {

    JSONObject updateJson = new JSONObject(update);

		Optional<Transaction> data = transactionRepository.findById(id);
		if (!data.isPresent()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Transaction transaction = data.get();
    JSONObject trdata = new JSONObject(transaction.getData());

    if (updateJson.has("amount")) {
      trdata.put("amount", updateJson.getInt("amount"));
    }

    if (updateJson.has(Transaction.REFERENCE_KEY)) {
      trdata.put(Transaction.REFERENCE_KEY, updateJson.getString(Transaction.REFERENCE_KEY));
    }
    transaction.setData(trdata.toString());

		Transaction updatedTransaction = transactionRepository.save(transaction);
		return ResponseEntity.ok(updatedTransaction);
	}
}