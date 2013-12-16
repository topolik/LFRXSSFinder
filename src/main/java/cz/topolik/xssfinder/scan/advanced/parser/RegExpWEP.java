package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;

import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class RegExpWEP implements WhitelistExpressionParser {
    private Pattern regExp;

    public RegExpWEP(String pattern) {
        regExp = Pattern.compile(pattern);
    }

    @Override
    public boolean isSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        return regExp.matcher(expression).matches();
    }
}
