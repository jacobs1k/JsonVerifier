package JsonVerifier;

public class JsonVerificationException extends Exception {
    public JsonVerificationException(String message) {
        super(message);
    }

    public JsonVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
