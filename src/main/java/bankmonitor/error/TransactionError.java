package bankmonitor.error;

sealed public interface TransactionError permits DTOError, GeneralError {
    default int errorCode() {
        return 400;
    }
}

