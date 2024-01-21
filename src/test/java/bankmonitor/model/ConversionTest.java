package bankmonitor.model;

import bankmonitor.service.Conversions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ConversionTest {

    @Autowired
    Conversions conversions;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void makeTransactionFromDTO() {
        var dto = new TransactionDTO(1, LocalDateTime.now(),
                    """
                        { "amount": -100, "reference": "BM_2023_101_BACK", "reason": "duplicate" }
                        """);

        // expect no exception
        assertDoesNotThrow(() -> {
            var transaction = conversions.fromDTO(dto);
            assertEquals(1, transaction.getId());
            assertEquals(transaction.getId(), transaction.getTransactionData().getId());
            assertEquals(-100, transaction.getTransactionData().getDetails().getAmount());
            assertEquals("BM_2023_101_BACK", transaction.getTransactionData().getDetails().getReference());
            assertEquals("duplicate", transaction.getTransactionData().getDetails().getReason());
            assertNull(transaction.getTransactionData().getDetails().getSender());
            assertNull(transaction.getTransactionData().getDetails().getRecipient());
        });

    }

    @Test
    void makeDTOFromTransaction() {
        var transaction = TransactionV2.builder()
                .id(1L)
                .timestamp(LocalDateTime.now())
                .transactionData(TransactionData.builder()
                        .id(1L)
                        .details(TransactionDataDTO.builder()
                                .amount(-100)
                                .reference("BM_2023_101_BACK")
                                .reason("duplicate")
                                .build())
                        .build())
                .build();

        assertDoesNotThrow(() -> {
            var dto = conversions.toDTO(transaction);
            JSONObject jsonData = new JSONObject(dto.getData());
            assertEquals(-100, jsonData.getInt("amount"));
            assertEquals("BM_2023_101_BACK", jsonData.getString("reference"));
            assertEquals("duplicate", jsonData.getString("reason"));
            assertTrue(jsonData.isNull("sender"));
            assertTrue(jsonData.isNull("recipient"));
        });
    }

    @Test
    public void deserializeEmbeddable() throws JsonProcessingException {
        var json = """
                {
                    "amount": 100,
                    "reference": "test reference",
                    "sender": "test sender",
                    "recipient": "test recipient",
                    "reason": "test reason"
                }
                """;

        var embeddable = new  ObjectMapper().readValue(json, TransactionDataDTO.class);
        assertThat(embeddable.getAmount(), is(100));
        assertThat(embeddable.getReference(), is("test reference"));
        assertThat(embeddable.getSender(), is("test sender"));
        assertThat(embeddable.getRecipient(), is("test recipient"));
        assertThat(embeddable.getReason(), is("test reason"));
    }

    @Test
    public void deserializeDto() throws JsonProcessingException {
        var dto = new TransactionDTO(1, LocalDateTime.now(),
                """
                    { "amount": -100, "reference": "BM_2023_101_BACK", "reason": "duplicate" }
                    """);

        var result = objectMapper.writeValueAsString(dto);
        System.out.println("result = " + result);
    }
}