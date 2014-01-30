package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSLogicProcessorHelperUtilThingie;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.List;

/**
 *
 * @author Tomas Polesovsky
 */
public interface ColoredButterfly {
    public static List<String> RESULT_DONT_KNOW = XSSLogicProcessorHelperUtilThingie.RESULT_DONT_KNOW;
    public static List<String> RESULT_SAFE = XSSLogicProcessorHelperUtilThingie.RESULT_SAFE;

    public List<String> execute(Droplet droplet);
}
