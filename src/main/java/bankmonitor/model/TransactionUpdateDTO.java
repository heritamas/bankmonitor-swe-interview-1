package bankmonitor.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@Jacksonized
public class TransactionUpdateDTO {

    @NotBlank
    private String reference;

    @NotNull
    private Integer amount;
}
