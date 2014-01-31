package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class REReplaceRainbowFish implements RainbowFish {
    Pattern regExp;
    String replacement;

    public REReplaceRainbowFish(String regExp, String replaceRegExp) {
        this.regExp = Pattern.compile(regExp);
        this.replacement = replaceRegExp;
    }

    @Override
    public List<String> swallow(Droplet droplet) {
        Matcher m = regExp.matcher(droplet.getExpression());
        if (!m.matches()) {
            return UNEATABLE;
        }

        String newReplacement = m.replaceAll(replacement);

        return World.see().river().isCallArgumentSuspected(droplet.droppy(newReplacement));
    }

}
