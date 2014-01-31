package cz.topolik.xssfinder.v2.animals.butterfly;

import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class JSPContextAttributeFinderColoredButterfly implements ColoredButterfly {
    Pattern findAttributeRE = Pattern.compile("^_jspx_page_context\\.findAttribute\\((\"[^\\\"]+\")\\)");
    String replacement = "([\\w0-9_]+).setVar\\($1\\)";

    @Override
    public List<String> execute(Droplet droplet) {
        Matcher m = findAttributeRE.matcher(droplet.getExpression());
        if (!m.matches()) {
            return RESULT_DONT_KNOW;
        }

        String newReplacement = replacement;
        for (int i = 1; i <= m.groupCount(); i++) {
            String group = m.group(i);
            newReplacement = replacement.replaceAll(Pattern.quote("$" + i), Matcher.quoteReplacement(Pattern.quote(group)));
        }

        REColoredButterfly replacementRegExpCEP = new REColoredButterfly(newReplacement, new int[]{1});

        // GO UP from current line to find the variable
        for (int i = droplet.getGrowthRingNum() - 1; i >= 0; i--) {
            String fileLine = droplet.getTree().getGrowthRings().get(i).trim().replaceAll(";", "");

            List<String> replacementResult = replacementRegExpCEP.execute(droplet.droppy(fileLine, i, fileLine));
            if (replacementResult == RESULT_DONT_KNOW) {
                continue;
            }

            return replacementResult;
        }

        return RESULT_DONT_KNOW;
    }


}
