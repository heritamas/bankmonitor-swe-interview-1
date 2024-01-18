package bankmonitor.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.Embeddable;

@Embeddable
@Data
@Builder
@Jacksonized
public class TransactionDataEmbeddable {
    private Integer amount;
    private String reference;
    private String sender;
    private String recipient;
    private String reason;
}
