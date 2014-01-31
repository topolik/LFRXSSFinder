package cz.topolik.xssfinder.v2.sun;

/**
 * @author Tomas Polesovsky
 */
public class SunShineException extends RuntimeException {
    public SunShineException() {
        super();
    }

    public SunShineException(String message) {
        super(message);
    }

    public SunShineException(String message, Throwable cause) {
        super(message, cause);
    }

    public SunShineException(Throwable cause) {
        super(cause);
    }
}
