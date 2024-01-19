package bankmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.Embeddable;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class TransactionDataEmbeddable {
    private Integer amount;
    private String reference;
    private String sender;
    private String recipient;
    private String reason;
}
