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
public class SBRainbowFish implements RainbowFish {
    Pattern variableDeclaration;
    Pattern SB_APPEND = Pattern.compile("sb\\.append\\((.*)\\);");

    public SBRainbowFish() {
        variableDeclaration = World.see().river().buildVariableDeclaration("sb");
    }

    @Override
    public List<String> swallow(Droplet droplet) {
        if (!droplet.getExpression().equals("sb.toString()")) {
            return UNEATABLE;
        }

        List<String> result = new ArrayList<String>();
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

                List<String> callResult = World.see().river().isCallArgumentSuspected(droplet.droppy(arg));
                if (callResult != TASTY) {
                    everythingOK = false;
                    result.add(fileLine);
                    if (callResult.size() > 0) {
                        result.addAll(callResult);
                    }
                }
                continue;
            }

            if (variableDeclaration.matcher(fileLine).matches() || fileLine.startsWith("sb = new ")) {
                // stop searching to avoid collision with another variable with the same name
                return everythingOK ? TASTY : result;
            }

            if (fileLine.contains("sb") && !fileLine.contains("sb.setIndex(") && !fileLine.contains("sb.index()") && !fileLine.contains("sb.toString()")) {
                result.add(fileLine);
                return result;
            }
        }

        return everythingOK ? TASTY : result;
    }
}
