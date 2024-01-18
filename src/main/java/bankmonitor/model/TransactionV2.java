package bankmonitor.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionV2 {

    private Long id;

    private LocalDateTime timestamp;

    private String data;

    private TransactionData transactionData;
}
