package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Water;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class BeanRainbowFish implements RainbowFish {
    static final Pattern EXPRESSION = Pattern.compile("^([a-z][\\w]+)\\.([\\w]+)\\(.*\\)$");

    @Override
    public Water swallow(Droplet droplet) {
        Matcher m = EXPRESSION.matcher(droplet.getExpression());
        if (!m.matches()) {
            return Water.UNKNOWN_WATER;
        }

        String beanName = m.group(1).trim();
        String methodName = m.group(2).trim();
        String fullClassName = null;

        Pattern variableDeclaration = Pattern.compile("^(for ?\\()?([^\\s]+) " + beanName + " (=|:).*$");
        String declarationLine = null;
        for (int i = droplet.getRingNum() - 1; i >= 0 && declarationLine == null; i--) {
            String fileLine = droplet.getRing(i);
            if (fileLine.startsWith("out.write") || fileLine.startsWith("out.print")) {
                continue;
            }
            Matcher m2 = variableDeclaration.matcher(fileLine);
            if (m2.matches()) {
                fullClassName = m2.group(2);
                declarationLine = fileLine;
            }
        }

        if (fullClassName != null) {
            String simpleName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            if (World.see().rain().shed(droplet.droppy(simpleName + "." + methodName + "("))) {
                return Water.CLEAN_WATER;
            } else {
                Water result = new Water();
                result.add(declarationLine);
                return result;
            }
        }

        return Water.UNKNOWN_WATER;
    }
}
