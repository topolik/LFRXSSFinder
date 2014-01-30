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
public class REColoredButterfly implements ColoredButterfly {
    Pattern regExp;
    int[] groups;


    public REColoredButterfly(String regExp, int[] groups) {
        this.regExp = Pattern.compile(regExp);
        this.groups = groups;
    }

    @Override
    public List<String> execute(Droplet droplet) {
        return execute(droplet, droplet.getExpression(), droplet.getGrowthRingNum(), droplet.getGrowthRing(), droplet.getFileContent());
    }

    public List<String> execute(Droplet droplet, String expression, int lineNum, String line, FileContent f){
        Matcher m = regExp.matcher(expression);
        if(!m.matches()){
            return RESULT_DONT_KNOW;
        }
        
        List<String> result = new ArrayList<String>();
        boolean everythingOK = true;
        for(int i = 0; i < groups.length; i++){
            if(groups[i] > m.groupCount()){
                continue;
            }

            String arg = m.group(groups[i]);
            List<String> callResult = World.see().river().isCallArgumentSuspected(new Droplet(arg, lineNum, line, f));
            if(callResult != RESULT_SAFE){
                everythingOK = false;
                if(callResult.size() > 0){
                    result.addAll(callResult);
                } else {
                    result.add(arg);
                }
            }
        }

        if(everythingOK){
            // it's safe dude
            return RESULT_SAFE;
        }
        
        return result;
    }
}
