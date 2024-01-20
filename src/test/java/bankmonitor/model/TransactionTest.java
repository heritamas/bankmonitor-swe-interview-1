package bankmonitor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TransactionTest {

  @Test
  void test_getData() {
    Transaction tr = new Transaction("{ \"reference\": \"foo\", \"amount\": 100}");

    assertEquals(tr.getReference(), "foo");
    assertEquals(tr.getAmount(), 100);
  }
}