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
public class RERainbowFish implements RainbowFish {
    Pattern regExp;
    int[] groups;


    public RERainbowFish(String regExp, int[] groups) {
        this.regExp = Pattern.compile(regExp);
        this.groups = groups;
    }

    @Override
    public Water swallow(Droplet droplet) {
        Matcher m = regExp.matcher(droplet.getExpression());
        if (!m.matches()) {
            return Water.UNKNOWN_WATER;
        }

        Water result = new Water();
        boolean everythingOK = true;
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] > m.groupCount()) {
                continue;
            }

            String arg = m.group(groups[i]);
            Water callResult = droplet.droppy(arg).dryUp();
            if (!callResult.equals(Water.CLEAN_WATER)) {
                everythingOK = false;
                if (callResult.size() > 0) {
                    result.add(callResult);
                } else {
                    result.add(arg);
                }
            }
        }

        if (everythingOK) {
            // it's safe dude
            return Water.CLEAN_WATER;
        }

        return result;
    }
}
