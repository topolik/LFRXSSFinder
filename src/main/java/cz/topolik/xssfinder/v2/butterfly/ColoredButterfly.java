package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.River;

import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public interface ColoredButterfly {
    public static List<String> RESULT_DONT_KNOW = River.RESULT_DONT_KNOW;
    public static List<String> RESULT_SAFE = River.RESULT_SAFE;

    public List<String> execute(Droplet droplet);
}
