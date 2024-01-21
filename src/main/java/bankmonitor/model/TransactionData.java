package bankmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


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


