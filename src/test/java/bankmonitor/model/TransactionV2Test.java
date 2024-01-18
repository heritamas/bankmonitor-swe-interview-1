package bankmonitor.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class TransactionV2Test {


    @Test
    void makeTransactionfromDTO() {
        var dto = new TransactionDTO(1, LocalDateTime.now(),
                    """
                        { "amount": -100, "reference": "BM_2023_101_BACK", "reason": "duplicate" }
                        """);

        // expect no exception
        assertDoesNotThrow(() -> {
            var transaction = TransactionV2.of(dto);
            assertEquals(transaction.getData().getAmount(), -100);
        });

    }
}