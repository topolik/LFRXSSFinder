package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class REReplaceColoredButtefly implements ColoredButterfly {
    Pattern regExp;
    String replacement;

    public REReplaceColoredButtefly(String regExp, String replaceRegExp) {
        this.regExp = Pattern.compile(regExp);
        this.replacement = replaceRegExp;
    }

    @Override
    public List<String> execute(Droplet droplet) {
        return execute(droplet, droplet.getExpression(), droplet.getGrowthRingNum(), droplet.getGrowthRing(), droplet.getFileContent());
    }

    public List<String> execute(Droplet droplet, String expression, int lineNum, String line, FileContent f) {
        Matcher m = regExp.matcher(expression);
        if(!m.matches()){
            return RESULT_DONT_KNOW;
        }

        String newReplacement = m.replaceAll(replacement);

        return World.see().river().isCallArgumentSuspected(new Droplet(newReplacement, lineNum, line, f));
    }

}
