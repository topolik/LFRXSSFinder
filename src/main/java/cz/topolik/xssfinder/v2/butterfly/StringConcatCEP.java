package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tomas Polesovsky
 */
public class StringConcatCEP implements ColoredButterfly {
    static final Pattern EXPRESSION = Pattern.compile("^([^\\+]+\\+)+([^\\+]+)$");

    @Override
    public List<String> execute(Droplet droplet) {
        return execute(droplet, droplet.getExpression(), droplet.getGrowthRingNum(), droplet.getGrowthRing(), droplet.getFileContent());
    }

    public List<String> execute(Droplet droplet, String expression, int lineNum, String line, FileContent f) {
        Matcher m = EXPRESSION.matcher(expression);
        if (!m.matches()) {
            return RESULT_DONT_KNOW;
        }
        
        String[] args = expression.split("\\+");
        List<String> results = new ArrayList<String>();
        boolean everythingOK = true;
        for (String arg : args) {
            List<String> callResult =  World.see().river().isCallArgumentSuspected(new Droplet(arg, lineNum, line, f));
            if(callResult != RESULT_SAFE){
                everythingOK = false;
                if(callResult.size() > 0){
                    results.addAll(callResult);
                } else {
                    results.add(arg);
                }
            }
        }

        if (everythingOK) {
            return RESULT_SAFE;
        }
        
        return results;
    }
}
