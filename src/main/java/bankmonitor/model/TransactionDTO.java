package bankmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
public class TransactionDTO {
	private long id;

	private LocalDateTime timestamp;

	@NotBlank(message = "Data cannot be blank")
	private String data;
}