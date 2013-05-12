package cz.topolik.xssfinder.scan;

/**
 *
 * @author Tomas Polesovsky
 */
public class Logger {
    private static final long time = System.currentTimeMillis();
    public static void log(String msg){
        long t = (System.currentTimeMillis() - time);
        System.out.println((t / 1000) + "." + (t % 1000) + ": " + msg);
        System.out.flush();
    }
}
