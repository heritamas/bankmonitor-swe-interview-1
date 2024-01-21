package bankmonitor.advice;

import bankmonitor.error.ApiErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackageClasses = ErrorAdvice.class)
public class ErrorAdvice {

    @Autowired
    ObjectMapper objectMapper;

    @ExceptionHandler
    public String handleApiError(ApiErrorException e) {
        try {
            return objectMapper.writeValueAsString(e);
        } catch (JsonProcessingException ex) {
            return "Unable to serialize error message";
        }
    }
}
