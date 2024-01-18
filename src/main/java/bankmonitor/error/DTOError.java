package bankmonitor.error;

public sealed interface DTOError extends TransactionError {
    record InvalidJSON(String message) implements DTOError { }
    record NotFound(Long id) implements DTOError { }
}
