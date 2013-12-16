package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class JSPContextAttributeFinderCEP implements ComplexExpressionParser {
    Pattern findAttributeRE = Pattern.compile("^_jspx_page_context\\.findAttribute\\((\"[^\\\"]+\")\\)");
    String replacement = "([\\w0-9_]+).setVar\\($1\\)";
    XSSEnvironment environment;

    public JSPContextAttributeFinderCEP(XSSEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        Matcher m = findAttributeRE.matcher(expression);
        if(!m.matches()){
            return RESULT_DONT_KNOW;
        }

        String newReplacement = replacement;
        for(int i = 1; i <= m.groupCount(); i++){
            String group = m.group(i);
            newReplacement = replacement.replaceAll(Pattern.quote("$"+i), Matcher.quoteReplacement(Pattern.quote(group)));
        }

        RegExpCEP replacementRegExpCEP = new RegExpCEP(newReplacement, new int[]{1}, environment);

        // GO UP from current line to find the variable
        for (int i = lineNum - 1; i >= 0; i--) {
            String fileLine = f.getContent().get(i).trim().replaceAll(";", "");

            List<String> replacementResult = replacementRegExpCEP.execute(fileLine, i, fileLine, f, loader);
            if(replacementResult == RESULT_DONT_KNOW) {
                continue;
            }

            return replacementResult;
        }

        return RESULT_DONT_KNOW;
    }


}
