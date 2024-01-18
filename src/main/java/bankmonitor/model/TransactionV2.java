package bankmonitor.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
public class TransactionV2 {

    @Autowired
    private static ObjectMapper objectMapper;

    @Data
    @Builder
    @Jacksonized
    public static class TransactionData {
        private Integer amount;
        private String reference;
        private String sender;
        private String recipient;
        private String reason;
    }

    private Long id;

    private LocalDateTime timestamp;

    private TransactionData data;

    // parse Json to build valid TransactionV2 object
    public static TransactionV2 of(TransactionDTO dto) throws JsonProcessingException {
        TransactionV2Builder builder = TransactionV2.builder();
        builder
                .id(dto.getId())
                .timestamp(dto.getTimestamp())
                .data(objectMapper.readValue(dto.getData(), TransactionData.class));

        return builder.build();
    }
}
