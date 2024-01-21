package bankmonitor.error;

import bankmonitor.controller.TransactionControllerV2;

public class ApiErrorException extends Exception {

    int code;
    public ApiErrorException(int code, String message) {
        super(message);
        this.code = code;
    }
}
