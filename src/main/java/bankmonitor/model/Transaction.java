package bankmonitor.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.jackson.Jacksonized;
import org.json.JSONObject;

import javax.persistence.*;

import static javax.persistence.InheritanceType.JOINED;

@Entity
@Table(name = "transaction")
@Data
@Builder
@AllArgsConstructor
@Jacksonized
public class Transaction {

    public static final String REFERENCE_KEY = "reference";
    public static final String AMOUNT_KEY = "amount";

    private static String getJsonString(String jsonData, String key, String defaultValue) {
        JSONObject jsonObject = new JSONObject(jsonData);
        if (jsonObject.has(key)) {
            return jsonObject.getString(key);
        } else {
            return defaultValue;
        }
    }

    private static Integer getJsonInt(String jsonData, String key, Integer defaultValue) {
        JSONObject jsonObject = new JSONObject(jsonData);
        if (jsonObject.has(key)) {
            return jsonObject.getInt(key);
        } else {
            return defaultValue;
        }
    }

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
        return Transaction.getJsonInt(this.data, AMOUNT_KEY, -1);
    }

    public String getReference() {
        return Transaction.getJsonString(this.data, REFERENCE_KEY, "");
    }
}