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
 * @author Tomas Polesovsky
 */
public class StringBundlerCEP implements ColoredButterfly {
    Pattern variableDeclaration;
    Pattern SB_APPEND = Pattern.compile("sb\\.append\\((.*)\\);");

    public StringBundlerCEP() {
        variableDeclaration =  World.see().river().buildVariableDeclaration("sb");
    }

    @Override
    public List<String> execute(Droplet droplet) {
        return execute(droplet, droplet.getExpression(), droplet.getGrowthRingNum(), droplet.getGrowthRing(), droplet.getFileContent());
    }

    public List<String> execute(Droplet droplet, String expression, int lineNum, String line, FileContent f) {
        if(!expression.equals("sb.toString()")) {
            return RESULT_DONT_KNOW;
        }

        List<String> result = new ArrayList<String>();
        boolean everythingOK = true;
        boolean insideComment = false;
        for (int i = lineNum - 1; i >= 0; i--) {
            String fileLine = f.getContent().get(i).trim();

            if(fileLine.endsWith("*/")) {
                insideComment = true;
            }
            if(insideComment){
                if(fileLine.startsWith("/*")) {
                    insideComment = false;
                }

                continue;
            }

            Matcher m = SB_APPEND.matcher(fileLine);
            if (m.matches()){
                String arg = m.group(1);

                List<String> callResult =  World.see().river().isCallArgumentSuspected(new Droplet(arg, lineNum, line, f));
                if (callResult != RESULT_SAFE){
                    everythingOK = false;
                    result.add(fileLine);
                    if (callResult.size() > 0){
                        result.addAll(callResult);
                    }
                }
                continue;
            }

            if (variableDeclaration.matcher(fileLine).matches() || fileLine.startsWith("sb = new ")){
                // stop searching to avoid collision with another variable with the same name
                return everythingOK ? RESULT_SAFE : result;
            }

            if (fileLine.contains("sb") && !fileLine.contains("sb.setIndex(") && !fileLine.contains("sb.index()") && !fileLine.contains("sb.toString()")){
                result.add(fileLine);
                return result;
            }
        }

        return everythingOK ? RESULT_SAFE : result;
    }
}
