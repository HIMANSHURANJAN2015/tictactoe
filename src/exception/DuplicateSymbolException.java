package exception;

public class DuplicateSymbolException extends RuntimeException{
    public DuplicateSymbolException() {
    }

    public DuplicateSymbolException(String message) {
        super(message);
    }
}
