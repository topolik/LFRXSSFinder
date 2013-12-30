package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class EscapedModelWEP implements WhitelistExpressionParser {
    /*
     * Use only variables that don't start with underscore
     */
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("[a-zA-Z][_a-zA-Z0-9]*");
    XSSEnvironment environment;

    public EscapedModelWEP(XSSEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public boolean isSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
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

        Pattern variableDeclaration = environment.getXSSLogicProcessorHelperUtilThingie().buildVariableDeclaration(expression);
        String escapedModel = expression + " = " + expression + ".toEscapedModel();";

        for (int i = lineNum - 1; i >= 0; i--) {
            String fileLine = f.getContent().get(i).trim();

            if (escapedModel.equals(fileLine)) {
                return true;
            }

            if (variableDeclaration.matcher(fileLine).matches()){
                // stop searching to avoid collision with another variable with the same name
                return false;
            }
        }

        return false;
    }
}
