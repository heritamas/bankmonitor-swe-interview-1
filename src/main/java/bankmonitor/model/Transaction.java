package bankmonitor.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.SneakyThrows;
import org.json.JSONObject;

@Entity
@Table(name = "transaction")
public class Transaction {

  public static final String REFERENCE_KEY = "reference";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "created_at")
	private LocalDateTime timestamp;

	@Column(name = "data")
	private String data;

  public Transaction(String jsonData) {
    this.timestamp = LocalDateTime.now();
    this.data = jsonData;
  }

  public String getData() {
    return this.data;
  }

  public Boolean setData(String data) {
    this.data = data;
    return true;
  }

  @SneakyThrows
  public Integer getAmount() {
    JSONObject jsonData = new JSONObject(this.data);
    if (jsonData.has("amount")) {
      return jsonData.getInt("amount");
    } else {
      return -1;
    }
  }

  @SneakyThrows
  public String getReference() {
    JSONObject jsonData = new JSONObject(this.data);
    if (jsonData.has(REFERENCE_KEY)) {
      return jsonData.getString(REFERENCE_KEY);
    } else {
      return "";
    }
  }
}