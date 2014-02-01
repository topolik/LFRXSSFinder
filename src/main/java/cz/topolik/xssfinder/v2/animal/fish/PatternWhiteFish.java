package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class PatternWhiteFish extends UsefulFish implements WhiteFish {
    private Pattern regExp;

    public PatternWhiteFish(String pattern) {
        regExp = Pattern.compile(pattern);
    }

    @Override
    public boolean likes(Droplet droplet) {
        return regExp.matcher(droplet.getExpression()).matches();
    }

    @Override
    public String toString() {
        return "PatternWhiteFish{" +
                "regExp=" + regExp +
                '}';
    }
}
