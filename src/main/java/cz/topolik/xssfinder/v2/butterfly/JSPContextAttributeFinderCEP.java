package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class JSPContextAttributeFinderCEP implements ColoredButterfly {
    Pattern findAttributeRE = Pattern.compile("^_jspx_page_context\\.findAttribute\\((\"[^\\\"]+\")\\)");
    String replacement = "([\\w0-9_]+).setVar\\($1\\)";

    @Override
    public List<String> execute(Droplet droplet) {
        return execute(droplet, droplet.getExpression(), droplet.getGrowthRingNum(), droplet.getGrowthRing(), droplet.getFileContent());
    }

    public List<String> execute(Droplet droplet, String expression, int lineNum, String line, FileContent f) {
        Matcher m = findAttributeRE.matcher(expression);
        if(!m.matches()){
            return RESULT_DONT_KNOW;
        }

        String newReplacement = replacement;
        for(int i = 1; i <= m.groupCount(); i++){
            String group = m.group(i);
            newReplacement = replacement.replaceAll(Pattern.quote("$"+i), Matcher.quoteReplacement(Pattern.quote(group)));
        }

        REColoredButterfly replacementRegExpCEP = new REColoredButterfly(newReplacement, new int[]{1});

        // GO UP from current line to find the variable
        for (int i = lineNum - 1; i >= 0; i--) {
            String fileLine = f.getContent().get(i).trim().replaceAll(";", "");

            List<String> replacementResult = replacementRegExpCEP.execute(new Droplet(fileLine, i, fileLine, f));
            if(replacementResult == RESULT_DONT_KNOW) {
                continue;
            }

            return replacementResult;
        }

        return RESULT_DONT_KNOW;
    }


}
