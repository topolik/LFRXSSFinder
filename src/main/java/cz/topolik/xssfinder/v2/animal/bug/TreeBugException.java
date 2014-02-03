package cz.topolik.xssfinder.v2.animal.bug;

/**
 * @author Tomas Polesovsky
 */
public class TreeBugException extends RuntimeException {
    public TreeBugException() {
    }

    public TreeBugException(String message) {
        super(message);
    }

    public TreeBugException(String message, Throwable cause) {
        super(message, cause);
    }

    public TreeBugException(Throwable cause) {
        super(cause);
    }
}
