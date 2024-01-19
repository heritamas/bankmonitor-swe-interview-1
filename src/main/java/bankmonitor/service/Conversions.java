package bankmonitor.service;

import bankmonitor.error.TransactionError;
import bankmonitor.model.TransactionDTO;
import bankmonitor.model.TransactionData;
import bankmonitor.model.TransactionDataEmbeddable;
import bankmonitor.model.TransactionV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class Conversions {

    private final ObjectMapper objectMapper;

    @Autowired
    public Conversions(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }

    public <T, R, E extends Throwable> Function<T, Either<TransactionError, R>> lift(ThrowingFunction<T, R, E> func,
                                                                                     Function <Throwable, TransactionError> errorSupplier) {
        return t -> Try.of(() -> func.apply(t))
                .toEither()
                .mapLeft(errorSupplier);
    }

    public TransactionV2 fromDTO(TransactionDTO dto) throws JsonProcessingException {
        TransactionV2.TransactionV2Builder builder = TransactionV2.builder();
        // data and transactionData remain in sync - dto.data is the source of truth
        builder
                .id(dto.getId())
                .timestamp(dto.getTimestamp())
                .data(dto.getData())
                .transactionData(fromJson(dto.getId(), dto.getData()));

        return builder.build();
    }

    public TransactionData fromJson(Long id, String json) throws JsonProcessingException {
        var embeddable = objectMapper.readValue(json, TransactionDataEmbeddable.class);
        return TransactionData.builder()
                .id(id)
                .details(embeddable)
                .build();
    }

    public TransactionDTO toDTO(TransactionV2 tr) throws JsonProcessingException {
        TransactionDTO.TransactionDTOBuilder builder = TransactionDTO.builder();
        // we assume the transactionData field as the source of truth
        builder
                .id(tr.getId())
                .timestamp(tr.getTimestamp())
                .data(toJSON(tr.getTransactionData().getDetails()));

        return builder.build();
    }

    public String toJSON(TransactionDataEmbeddable tr) throws JsonProcessingException {
        return objectMapper.writeValueAsString(tr);
    }
}
