package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSLogicProcessorHelperUtilThingie;
import java.util.List;

/**
 *
 * @author Tomas Polesovsky
 */
public interface ComplexExpressionParser {
    public static List<String> RESULT_DONT_KNOW = XSSLogicProcessorHelperUtilThingie.RESULT_DONT_KNOW;
    public static List<String> RESULT_SAFE = XSSLogicProcessorHelperUtilThingie.RESULT_SAFE;

    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader);
}
