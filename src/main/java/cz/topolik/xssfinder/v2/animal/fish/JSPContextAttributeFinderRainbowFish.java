package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class JSPContextAttributeFinderRainbowFish implements RainbowFish {
    Pattern findAttributeRE = Pattern.compile("^_jspx_page_context\\.findAttribute\\((\"[^\\\"]+\")\\)");
    String replacement = "([\\w0-9_]+).setVar\\($1\\)";

    @Override
    public List<String> swallow(Droplet droplet) {
        Matcher m = findAttributeRE.matcher(droplet.getExpression());
        if (!m.matches()) {
            return UNEATABLE;
        }

        String newReplacement = replacement;
        for (int i = 1; i <= m.groupCount(); i++) {
            String group = m.group(i);
            newReplacement = replacement.replaceAll(Pattern.quote("$" + i), Matcher.quoteReplacement(Pattern.quote(group)));
        }

        RERainbowFish replacementRegExpCEP = new RERainbowFish(newReplacement, new int[]{1});

        // GO UP from current line to find the variable
        for (int i = droplet.getRingNum() - 1; i >= 0; i--) {
            String fileLine = droplet.getRing(i).replaceAll(";", "");

            List<String> replacementResult = replacementRegExpCEP.swallow(droplet.droppy(fileLine, i, fileLine));
            if (replacementResult == UNEATABLE) {
                continue;
            }

            return replacementResult;
        }

        return UNEATABLE;
    }


}
