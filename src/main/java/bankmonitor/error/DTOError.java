package bankmonitor.error;

public sealed interface DTOError extends TransactionError {
    record InvalidJSON(String message) implements DTOError { }
    record NotFound(Long id) implements DTOError {
        public int errorCode() {
            return 404;
        }
    }
    record UpdateError(String message) implements DTOError {  }

    record SaveError (String message) implements DTOError {  }

}
