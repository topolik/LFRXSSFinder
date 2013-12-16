package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;

/**
 * @author Tomas Polesovsky
 */
public class SimplePrefixedWEP implements WhitelistExpressionParser {
    private String prefix;

    public SimplePrefixedWEP(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean isSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        return expression != null && expression.startsWith(prefix);
    }
}
