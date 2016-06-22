package jp.co.bigdata.exception;

public class InvalidArgumentException extends Exception {
    private static final long serialVersionUID = 7487852970557301159L;

    public InvalidArgumentException() {
        super();
    }

    public InvalidArgumentException(String message) {
        super(message);
    }
}
