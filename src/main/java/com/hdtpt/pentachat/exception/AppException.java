package com.hdtpt.pentachat.exception;

<<<<<<< HEAD
import org.springframework.http.HttpStatus;

=======
>>>>>>> c99ecabafa9ac82c979d4fa63bf5d7254224336b
/**
 * Custom exception for application errors
 */
public class AppException extends RuntimeException {
<<<<<<< HEAD
    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public AppException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
=======
    public AppException(String message) {
        super(message);
>>>>>>> c99ecabafa9ac82c979d4fa63bf5d7254224336b
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
<<<<<<< HEAD
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public HttpStatus getStatus() {
        return status;
=======
>>>>>>> c99ecabafa9ac82c979d4fa63bf5d7254224336b
    }
}
