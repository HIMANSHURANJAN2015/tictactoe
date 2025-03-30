package exception;

public class InvalidUndoCommandException extends RuntimeException{
    public InvalidUndoCommandException() {
    }

    public InvalidUndoCommandException(String message) {
        super(message);
    }
}
