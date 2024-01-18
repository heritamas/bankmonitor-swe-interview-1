package bankmonitor.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionDTO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "created_at")
	private LocalDateTime timestamp;

	@Column(name = "data")
	private String data;

  public TransactionDTO(String jsonData) {
    this.timestamp = LocalDateTime.now();
    this.data = jsonData;
  }
}