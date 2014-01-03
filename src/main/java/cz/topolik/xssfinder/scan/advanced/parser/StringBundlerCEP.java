package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class StringBundlerCEP implements ComplexExpressionParser {
    Pattern variableDeclaration;
    XSSEnvironment environment;
    Pattern SB_APPEND = Pattern.compile("sb\\.append\\((.*)\\);");

    public StringBundlerCEP(XSSEnvironment environment) {
        this.environment = environment;
        variableDeclaration = environment.getXSSLogicProcessorHelperUtilThingie().buildVariableDeclaration("sb");
    }


    @Override
    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
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

                List<String> callResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(arg, lineNum, line, f, loader);
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
