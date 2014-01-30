package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class PatternWhiteButtefly implements WhiteButterfly {
    private Pattern regExp;

    public PatternWhiteButtefly(String pattern) {
        regExp = Pattern.compile(pattern);
    }

    @Override
    public boolean isSafe(Droplet droplet) {
        return isSafe(droplet.getExpression(), droplet.getGrowthRingNum(), droplet.getGrowthRing(), droplet.getFileContent());
    }

    public boolean isSafe(String expression, int lineNum, String line, FileContent f) {
        return regExp.matcher(expression).matches();
    }
}
