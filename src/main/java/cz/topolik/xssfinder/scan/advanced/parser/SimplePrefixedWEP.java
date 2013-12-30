package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;

/**
 * @author Tomas Polesovsky
 */
public class SimplePrefixedWEP implements WhitelistExpressionParser {
    private static final char[] SIMPLE_EXPRESSION_WHITELIST = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', ' ', '_',
            ',', '(', ')', '.', '!', '[', ']'
    };

    private String prefix;

    public SimplePrefixedWEP(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean isSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        if (!isExpressionSimple(expression)) {
            return false;
        }

        return expression != null && expression.startsWith(prefix);
    }

    protected boolean isExpressionSimple(String expression) {
        boolean insideString = false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '"'){
                if(!insideString) {
                    insideString = true;
                }
                else if (i > 0 && expression.charAt(i-1) != '\\') {
                    insideString = false;
                }
                continue;
            }
            if (insideString) {
                continue;
            }

            boolean insideWhitelist = false;
            for (int j = 0; j < SIMPLE_EXPRESSION_WHITELIST.length && !insideWhitelist; j++) {
                insideWhitelist = c == SIMPLE_EXPRESSION_WHITELIST[j];
            }

            if (!insideWhitelist) {
                return false;
            }
        }

        return true;
    }

}
