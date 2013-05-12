package cz.topolik.xssfinder.scan.advanced.parser;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.XSSEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tomas Polesovsky
 */
public class BeanCallCEP implements ComplexExpressionParser {
    static final Pattern EXPRESSION = Pattern.compile("^([a-z][\\w]+)\\.([\\w]+)\\(.*\\)$");
    private XSSEnvironment environment;

    public BeanCallCEP(XSSEnvironment environment) {
        this.environment = environment;
    }

    public List<String> execute(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        Matcher m = EXPRESSION.matcher(expression);
        if (!m.matches()) {
            return RESULT_DONT_KNOW;
        }

        String beanName = m.group(1).trim();
        String methodName = m.group(2).trim();
        String fullClassName = null;

        Pattern variableDeclaration = Pattern.compile("^(for ?\\()?([^\\s]+) " + beanName + " (=|:).*$");
        String declarationLine = null;
        for (int i = lineNum - 1; i >= 0 && declarationLine == null; i--) {
            String fileLine = f.getContent().get(i).trim();
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
            if (environment.getPortalAPICallsProcessor().isExpressionSafe(simpleName + "." + methodName + "(")) {
                return RESULT_SAFE;
            } else {
                return Arrays.asList(new String[]{declarationLine});
            }
        }

        return RESULT_DONT_KNOW;
    }
}
