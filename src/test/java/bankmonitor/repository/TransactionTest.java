package bankmonitor.repository;

import bankmonitor.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TransactionTest {

  @Autowired
  LegacyTransactionRepository legacyTransactionRepository;

  @Test
  void test_getData() {
    Transaction tr = new Transaction("{ \"reference\": \"foo\", \"amount\": 100}");

    assertEquals(tr.getReference(), "foo");
    assertEquals(tr.getAmount(), 100);
  }

  @Test
  public void testFetchAll() {
    var transactions = legacyTransactionRepository.findAll();
    assertFalse(transactions.isEmpty());
  }

}