package bankmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "transactiondata")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionData {
    @Id
    private Long id;

    @Embedded
    private TransactionDataDTO details;
}


