package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.World;
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
public class SBRainbowFish implements RainbowFish {
    Pattern variableDeclaration;
    Pattern SB_APPEND = Pattern.compile("sb\\.append\\((.*)\\);");

    public SBRainbowFish() {
        variableDeclaration = World.see().river().buildVariableDeclaration("sb");
    }

    @Override
    public Water swallow(Droplet droplet) {
        if (!droplet.getExpression().equals("sb.toString()")) {
            return Water.UNKNOWN_WATER;
        }

        Water result = new Water();
        boolean everythingOK = true;
        boolean insideComment = false;
        for (int i = droplet.getRingNum() - 1; i >= 0; i--) {
            String fileLine = droplet.getRing(i);

            if (fileLine.endsWith("*/")) {
                insideComment = true;
            }
            if (insideComment) {
                if (fileLine.startsWith("/*")) {
                    insideComment = false;
                }

                continue;
            }

            Matcher m = SB_APPEND.matcher(fileLine);
            if (m.matches()) {
                String arg = m.group(1);

                Water callResult = droplet.droppy(arg).dryUp();
                if (!callResult.equals(Water.CLEAN_WATER)) {
                    everythingOK = false;
                    result.add(fileLine);
                    result.add(callResult);
                }
                continue;
            }

            if (variableDeclaration.matcher(fileLine).matches() || fileLine.startsWith("sb = new ")) {
                // stop searching to avoid collision with another variable with the same name
                return everythingOK ? Water.CLEAN_WATER : result;
            }

            if (fileLine.contains("sb") && !fileLine.contains("sb.setIndex(") && !fileLine.contains("sb.index()") && !fileLine.contains("sb.toString()")) {
                result.add(fileLine);
                return result;
            }
        }

        return everythingOK ? Water.CLEAN_WATER : result;
    }
}
