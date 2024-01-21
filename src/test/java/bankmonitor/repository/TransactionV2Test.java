package bankmonitor.repository;

import bankmonitor.repository.TransactionV2Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TransactionV2Test {

    @Autowired
    TransactionV2Repository transactionV2Repository;

    @Test
    public void testFetchAll() {
        var transactions = transactionV2Repository.findAll();
        assertFalse(transactions.isEmpty());
    }

}
