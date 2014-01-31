package cz.topolik.xssfinder.v2.animals.butterfly;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class EscapeWhiteButterfly implements WhiteButterfly {
    /*
     * Use only variables that don't start with underscore
     */
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("[a-zA-Z][_a-zA-Z0-9]*");

    @Override
    public boolean isSafe(Droplet droplet) {
        String expression = droplet.getExpression();

        boolean isVariableStart = expression.charAt(0) >= 97 && expression.charAt(0) <= 122;
        if (!isVariableStart) {
            return false;
        }

        int pos = expression.indexOf('.');
        if (pos > 0) {
            expression = expression.substring(0, pos);
        }

        if (!JAVA_VARIABLE_PATTERN.matcher(expression).matches()) {
            return false;
        }

        Pattern variableDeclaration = World.see().river().buildVariableDeclaration(expression);
        String escapedModel = expression + " = " + expression + ".toEscapedModel();";

        for (int i = droplet.getGrowthRingNum() - 1; i >= 0; i--) {
            String fileLine = droplet.getTree().getGrowthRings().get(i).trim();

            if (escapedModel.equals(fileLine)) {
                return true;
            }

            if (variableDeclaration.matcher(fileLine).matches()) {
                // stop searching to avoid collision with another variable with the same name
                return false;
            }
        }

        return false;
    }
}
