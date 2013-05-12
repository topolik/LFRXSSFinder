package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tomas Polesovsky
 */
public class RegExpCEP implements ComplexExpressionParser {
    Pattern regExp;
    int[] groups;
    XSSEnvironment environment;


    public RegExpCEP(String regExp, int[] groups, XSSEnvironment environment) {
        this.regExp = Pattern.compile(regExp);
        this.groups = groups;
        this.environment = environment;
    }

    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader){
        Matcher m = regExp.matcher(expression);
        if(!m.matches()){
            return RESULT_DONT_KNOW;
        }
        
        List<String> result = new ArrayList<String>();
        boolean everythingOK = true;
        for(int i = 0; i < groups.length; i++){
            String arg = m.group(groups[i]);
            List<String> callResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(arg, lineNum, line, f, loader);
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
