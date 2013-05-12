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
public class StringConcatCEP implements ComplexExpressionParser {
    static final Pattern EXPRESSION = Pattern.compile("^([^\\+]+\\+)+([^\\+]+)$");
    private XSSEnvironment environment;

    public StringConcatCEP(XSSEnvironment environment) {
        this.environment = environment;
    }

    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        Matcher m = EXPRESSION.matcher(expression);
        if (!m.matches()) {
            return RESULT_DONT_KNOW;
        }
        
        String[] args = expression.split("\\+");
        List<String> results = new ArrayList<String>();
        boolean everythingOK = true;
        for (String arg : args) {
            List<String> callResult = environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(arg, lineNum, line, f, loader);
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
