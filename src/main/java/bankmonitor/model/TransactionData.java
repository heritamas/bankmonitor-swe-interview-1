package bankmonitor.model;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "transactiondata")
@Data
@Builder
public class TransactionData {
    @Id
    private Long id;

    @Embedded
    private TransactionDataEmbeddable details;
}


