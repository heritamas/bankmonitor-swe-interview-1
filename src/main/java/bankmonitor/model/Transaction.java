package bankmonitor.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.json.JSONObject;

import javax.persistence.*;

import static javax.persistence.InheritanceType.JOINED;

@Entity
@Table(name = "transaction")
@Inheritance(strategy=JOINED)
@Data
@Builder
@AllArgsConstructor
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

    public Transaction() {
        this.timestamp = LocalDateTime.now();
        this.data = "";
    }

    public String getData() {
        return this.data;
    }

    public Boolean setData(String data) {
        this.data = data;
        return true;
    }

    public Integer getAmount() {
        JSONObject jsonData = new JSONObject(this.data);
        if (jsonData.has("amount")) {
            return jsonData.getInt("amount");
        } else {
            return -1;
        }
    }

    public String getReference() {
        JSONObject jsonData = new JSONObject(this.data);
        if (jsonData.has(REFERENCE_KEY)) {
            return jsonData.getString(REFERENCE_KEY);
        } else {
            return "";
        }
    }
}