package bankmonitor.error;

sealed public interface GeneralError extends TransactionError {
    record General(String message) implements GeneralError {
        public int errorCode() {
            return 500;
        }
    }
}
