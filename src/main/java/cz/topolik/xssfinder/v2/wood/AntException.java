package cz.topolik.xssfinder.v2.wood;

/**
 * @author Tomas Polesovsky
 */
public class AntException extends RuntimeException {
    public AntException() {
        super();
    }

    public AntException(String message) {
        super(message);
    }

    public AntException(String message, Throwable cause) {
        super(message, cause);
    }

    public AntException(Throwable cause) {
        super(cause);
    }
}
