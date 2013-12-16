package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSLogicProcessorHelperUtilThingie;

import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public interface WhitelistExpressionParser {

    public boolean isSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader);

}
