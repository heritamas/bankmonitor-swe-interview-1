package bankmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TransactionDTO {
	private long id;
	private LocalDateTime timestamp;
	private String data;
}