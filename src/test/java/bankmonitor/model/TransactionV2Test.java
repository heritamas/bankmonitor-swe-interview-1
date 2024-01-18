package bankmonitor.model;

import bankmonitor.service.Conversions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class TransactionV2Test {

    @Autowired
    Conversions conversions;

    @Test
    void makeTransactionFromDTO() {
        var dto = new TransactionDTO(1, LocalDateTime.now(),
                    """
                        { "amount": -100, "reference": "BM_2023_101_BACK", "reason": "duplicate" }
                        """);

        // expect no exception
        assertDoesNotThrow(() -> {
            var transaction = conversions.fromDTO(dto);
            assertEquals(transaction.getTransactionData().getAmount(), -100);
            assertEquals(transaction.getTransactionData().getReference(), "BM_2023_101_BACK");
            assertEquals(transaction.getTransactionData().getReason(), "duplicate");
        });

    }

    @Test
    void makeDTOFromTransaction() {
        var transaction = TransactionV2.builder()
                .id(1L)
                .timestamp(LocalDateTime.now())
                .transactionData(TransactionData.builder()
                        .amount(-100)
                        .reference("BM_2023_101_BACK")
                        .reason("duplicate")
                        .build())
                .build();

        assertDoesNotThrow(() -> {
            var dto = conversions.toDTO(transaction);
            JSONObject jsonData = new JSONObject(dto.getData());
            assertEquals(jsonData.getInt("amount"), -100);
            assertEquals(jsonData.getString("reference"), "BM_2023_101_BACK");
            assertEquals(jsonData.getString("reason"), "duplicate");
        });
    }
}