package bankmonitor.service;

import bankmonitor.model.Transaction;
import bankmonitor.model.TransactionDataDTO;
import bankmonitor.repository.LegacyTransactionRepository;
import io.vavr.control.Either;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    TransactionService transactionService;

    @Autowired
    LegacyTransactionRepository legacyTransactionRepository;

    @Test
    public void testFetchAll() {
        // we have non-zero number of transactions
        var transactions = transactionService.getAllTransactions();
        assertFalse(transactions.isEmpty());
    }

    @Test
    public void testFetchedDataAreConsistent() {
        // fetch all transactions - ignore errors
        var transactions = transactionService.getAllTransactions().stream().filter(Either::isRight).map(Either::get).toList();
        assertFalse(transactions.isEmpty());

        assertDoesNotThrow(() -> {
            // every item in transactions should have the same number in id and transactiondata.id
            for (var transaction : transactions) {
                assertThat(transaction.getId(), is(transaction.getTransactionData().getId()));
                JSONObject data = new JSONObject(transaction.getData());
                if (data.has("reference")) {
                    assertThat(transaction.getTransactionData().getDetails().getReference(), is(data.getString("reference")));
                }
                if (data.has("sender")) {
                    assertThat(transaction.getTransactionData().getDetails().getSender(), is(data.getString("sender")));
                }
                if (data.has("recipient")) {
                    assertThat(transaction.getTransactionData().getDetails().getRecipient(), is(data.getString("recipient")));
                }
                if (data.has("reason")) {
                    assertThat(transaction.getTransactionData().getDetails().getReason(), is(data.getString("reason")));
                }
                if (data.has("amount")) {
                    assertThat(transaction.getTransactionData().getDetails().getAmount(), is(data.getInt("amount")));
                }
            }

        });
    }

    @Test
    public void testSaveTransaction() {
        // create a new transaction
        var dto = TransactionDataDTO.builder()
                .amount(100)
                .reference("test reference")
                .sender("test sender")
                .recipient("test recipient")
                .reason("test reason")
                .build();

        // save it
        var saved = transactionService.saveTransaction(dto);

        // check that it was saved
        assertThat(saved.isRight(), is(true));
        var savedTransaction = saved.get();

        // check that it is available
        var fetched = transactionService.findTransactionById(savedTransaction.getId());

        assertThat(fetched.isRight(), is(true));
        var fetchedTransaction = fetched.get();

        assertThat(fetchedTransaction, equalTo(savedTransaction));

        // check that the data is consistent
        assertThat(savedTransaction.getTransactionData().getDetails().getAmount(), is(100));
        assertThat(savedTransaction.getTransactionData().getDetails().getReference(), is("test reference"));
        assertThat(savedTransaction.getTransactionData().getDetails().getSender(), is("test sender"));
        assertThat(savedTransaction.getTransactionData().getDetails().getRecipient(), is("test recipient"));
        assertThat(savedTransaction.getTransactionData().getDetails().getReason(), is("test reason"));
    }


    @Test
    public void testCompatibilityWithLegacy() {
        var json = """
                {
                    "amount": 100,
                    "reference": "test reference",
                    "sender": "test sender",
                    "recipient": "test recipient",
                    "reason": "test reason"
                }
                """;
        var tr = new Transaction(json);
        var saved = legacyTransactionRepository.save(tr);

        // check that is can be refetched
        var fetched = transactionService.findTransactionById(saved.getId());
        assertThat(fetched.isRight(), is(true));
        var fetchedTransaction = fetched.get();

        // check that the data is consistent with the original
        assertThat(fetchedTransaction.getTransactionData().getDetails().getAmount(), is(100));
        assertThat(fetchedTransaction.getTransactionData().getDetails().getReference(), is("test reference"));
        assertThat(fetchedTransaction.getTransactionData().getDetails().getSender(), is("test sender"));
        assertThat(fetchedTransaction.getTransactionData().getDetails().getRecipient(), is("test recipient"));
        assertThat(fetchedTransaction.getTransactionData().getDetails().getReason(), is("test reason"));
    }


}
