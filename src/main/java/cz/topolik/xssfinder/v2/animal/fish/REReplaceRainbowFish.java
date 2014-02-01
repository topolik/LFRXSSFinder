package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Water;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class REReplaceRainbowFish extends UsefulFish implements RainbowFish {
    Pattern regExp;
    String replacement;

    public REReplaceRainbowFish(String regExp, String replaceRegExp) {
        this.regExp = Pattern.compile(regExp);
        this.replacement = replaceRegExp;
    }

    @Override
    public Water swallow(Droplet droplet) {
        Matcher m = regExp.matcher(droplet.getExpression());
        if (!m.matches()) {
            return Water.UNKNOWN_WATER;
        }

        String newReplacement = m.replaceAll(replacement);

        return droplet.droppy(newReplacement).dryUp();
    }

    @Override
    public String toString() {
        return "REReplaceRainbowFish{" +
                "regExp=" + regExp +
                ", replacement='" + replacement + '\'' +
                '}';
    }
}
