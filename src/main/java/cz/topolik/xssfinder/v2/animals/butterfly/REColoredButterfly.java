package cz.topolik.xssfinder.v2.animals.butterfly;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class REColoredButterfly implements ColoredButterfly {
    Pattern regExp;
    int[] groups;


    public REColoredButterfly(String regExp, int[] groups) {
        this.regExp = Pattern.compile(regExp);
        this.groups = groups;
    }

    @Override
    public List<String> execute(Droplet droplet) {
        Matcher m = regExp.matcher(droplet.getExpression());
        if (!m.matches()) {
            return RESULT_DONT_KNOW;
        }

        List<String> result = new ArrayList<String>();
        boolean everythingOK = true;
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] > m.groupCount()) {
                continue;
            }

            String arg = m.group(groups[i]);
            List<String> callResult = World.see().river().isCallArgumentSuspected(droplet.droppy(arg));
            if (callResult != RESULT_SAFE) {
                everythingOK = false;
                if (callResult.size() > 0) {
                    result.addAll(callResult);
                } else {
                    result.add(arg);
                }
            }
        }

        if (everythingOK) {
            // it's safe dude
            return RESULT_SAFE;
        }

        return result;
    }
}
