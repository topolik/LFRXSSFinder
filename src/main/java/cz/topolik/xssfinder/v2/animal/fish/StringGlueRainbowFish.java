package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Water;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class StringGlueRainbowFish implements RainbowFish {
    static final Pattern EXPRESSION = Pattern.compile("^([^\\+]+\\+)+([^\\+]+)$");

    @Override
    public Water swallow(Droplet droplet) {

        Matcher m = EXPRESSION.matcher(droplet.getExpression());
        if (!m.matches()) {
            return Water.UNKNOWN_WATER;
        }

        String[] args = droplet.getExpression().split("\\+");
        Water results = new Water();
        boolean everythingOK = true;
        for (String arg : args) {
            Water callResult = droplet.droppy(arg).dryUp();
            if (!callResult.equals(Water.CLEAN_WATER)) {
                everythingOK = false;
                if (callResult.size() > 0) {
                    results.add(callResult);
                } else {
                    results.add(arg);
                }
            }
        }

        if (everythingOK) {
            return Water.CLEAN_WATER;
        }

        return results;
    }
}
