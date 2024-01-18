package bankmonitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class TransactionUpdateDTO {

    @NotBlank
    @JsonProperty("reference")
    private String reference;


    @NotNull
    @JsonProperty("amount")
    private Integer amount;
}
