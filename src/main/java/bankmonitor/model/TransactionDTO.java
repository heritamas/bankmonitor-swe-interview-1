package bankmonitor.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
	@JsonSerialize(using = LiteralSerializer.class)
	private String data;
}