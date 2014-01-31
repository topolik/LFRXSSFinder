package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class StringGlueRainbowFish implements RainbowFish {
    static final Pattern EXPRESSION = Pattern.compile("^([^\\+]+\\+)+([^\\+]+)$");

    @Override
    public List<String> swallow(Droplet droplet) {

        Matcher m = EXPRESSION.matcher(droplet.getExpression());
        if (!m.matches()) {
            return UNEATABLE;
        }

        String[] args = droplet.getExpression().split("\\+");
        List<String> results = new ArrayList<String>();
        boolean everythingOK = true;
        for (String arg : args) {
            List<String> callResult = World.see().river().isCallArgumentSuspected(droplet.droppy(arg));
            if (callResult != TASTY) {
                everythingOK = false;
                if (callResult.size() > 0) {
                    results.addAll(callResult);
                } else {
                    results.add(arg);
                }
            }
        }

        if (everythingOK) {
            return TASTY;
        }

        return results;
    }
}
