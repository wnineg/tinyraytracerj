package personal.william.raytracer;

public class AlgebraException extends RuntimeException {
    public AlgebraException() {
    }

    public AlgebraException(String message) {
        super(message);
    }

    public AlgebraException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlgebraException(Throwable cause) {
        super(cause);
    }
}
