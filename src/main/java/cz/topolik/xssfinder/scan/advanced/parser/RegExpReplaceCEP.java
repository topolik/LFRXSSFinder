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
public class RegExpReplaceCEP implements ComplexExpressionParser {
    Pattern regExp;
    String replacement;
    XSSEnvironment environment;

    public RegExpReplaceCEP(String regExp, String replaceRegExp, XSSEnvironment environment) {
        this.regExp = Pattern.compile(regExp);
        this.replacement = replaceRegExp;
        this.environment = environment;
    }

    @Override
    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        Matcher m = regExp.matcher(expression);
        if(!m.matches()){
            return RESULT_DONT_KNOW;
        }

        String newReplacement = m.replaceAll(replacement);

        return environment.getXSSLogicProcessorHelperUtilThingie().isCallArgumentSuspected(newReplacement, lineNum, line, f, loader);
    }

}
