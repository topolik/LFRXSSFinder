package cz.topolik.xssfinder.scan.advanced;

import cz.topolik.xssfinder.scan.advanced.parser.RegExpCEP;
import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.scan.advanced.parser.BeanCallCEP;
import cz.topolik.xssfinder.scan.advanced.parser.ComplexExpressionParser;
import cz.topolik.xssfinder.scan.advanced.parser.StringConcatCEP;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tomas Polesovsky
 */
public class XSSLogicProcessorHelperUtilThingie {
    public static List<String> RESULT_DONT_KNOW = new ArrayList<String>();
    public static List<String> RESULT_SAFE = new ArrayList<String>();

    List<Pattern> SAFE_XSS_RE = new ArrayList<Pattern>();
    List<String> SAFE_API_CALLS = new ArrayList<String>();
    List<ComplexExpressionParser> complexExpressionParsers = new ArrayList<ComplexExpressionParser>();

    static final String SAFE_VARIABLE_DECLARATION_START = ".*(byte|short|int|long|float|double|boolean|(java.lang.)?(Byte|Short|Integer|Long|Float|Double|Boolean))(\\[\\])? ";
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("\\w+");
    private XSSEnvironment environment;

    public XSSLogicProcessorHelperUtilThingie(XSSEnvironment environment) {
        this.environment = environment;
        init();
    }

    protected void init(){
        /*
        (Arrays.asList(new ComplexExpressionParser[]{
                // logicExpr ? X : Y; X,Y are from {"whatever", StringPool.*} *
                new RegExpCEP("^[\\(]?(.*)\\?([^:]+):([^\\)]+)[\\)]?$", new int[]{2, 3}, environment),
                // LanguageUtil.format(pageContext, "x-is-not-a-display-type", displayStyle) *
                new RegExpCEP("^LanguageUtil.format\\([^\\,]+\\,[^\\,]+\\,(.+)\\)$", new int[]{1}, environment),
                // StringUtil.shorten(HtmlUtil.stripHtml(entry.getDescription()), pageAbstractLength) *
                new RegExpCEP("^StringUtil.shorten\\(([^\\,]+)\\,[^\\,]+\\)$", new int[]{1}, environment),
                new RegExpCEP("^StringUtil.merge\\(([^\\,]+)\\)$", new int[]{1}, environment),
                // classVariable.callSomething()
                new RegExpCEP("GetterUtil.getString\\((.*), ?\"[^\"]*\"\\)$", new int[]{1}, environment),
                new RegExpCEP("([\\w]+)\\.toString\\(\\)$", new int[]{1}, environment),
                new RegExpCEP("^([\\w]+)\\[[^\\]]+\\]$", new int[]{1}, environment),
                new RegExpCEP("^StringUtil.merge\\((.+),([^\\)]+)\\)$", new int[]{1,2}, environment),
            };
            */
        complexExpressionParsers.add(new BeanCallCEP(environment));
        complexExpressionParsers.add(new StringConcatCEP(environment));
        
        InputStream in = getClass().getResourceAsStream("/safe_expressions.txt");
        try {
            Scanner s = new Scanner(in);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if(line.startsWith("simple=")){
                    SAFE_API_CALLS.add(line.substring(line.indexOf("=")+1));
                } else
                if(line.startsWith("pattern=")){
                    SAFE_XSS_RE.add(Pattern.compile(line.substring(line.indexOf("=")+1)));
                } else
                if(line.startsWith("reg-exp-cep=")){
                    int groupsStart = line.indexOf("=")+1;
                    int groupsEnd = line.indexOf("=", groupsStart);
                    int reStart = groupsEnd + 1;
                    String[] groupsStr = line.substring(groupsStart, groupsEnd).split(",");
                    String regExp = line.substring(reStart);
                    int[] groups = new int[groupsStr.length];
                    for(int i = 0; i < groupsStr.length; i++){
                        groups[i] = Integer.parseInt(groupsStr[i]);
                    }
                    complexExpressionParsers.add(new RegExpCEP(regExp, groups, environment));
                }
            }
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException ex) {}
            }
        }
    }

    public List<String> isCallArgumentSuspected(String arg, int lineNum, String line, FileContent f, FileLoader loader) {
        String argument = arg.trim();
        while (argument.endsWith(";")) {
            argument = argument.substring(0, argument.length() - 1);
        }

        if (isExpressionSafe(argument)) {
            return RESULT_SAFE;
        }

        List<String> simpleVariableResult = findVariableDeclaration(lineNum, argument, f, loader);
        if (simpleVariableResult == RESULT_SAFE) {
            // it's safe :)
            return RESULT_SAFE;
        }
        if(simpleVariableResult != RESULT_DONT_KNOW){



            // OK, it was a variable we don't need to continue
            return simpleVariableResult;
        }

        // OK, so we have more complex call :(
        List<String> complexResult = brakeComplexExpression(argument, lineNum, line, f, loader);
        if (complexResult == RESULT_SAFE) {
            // huh, it's safe
            return RESULT_SAFE;
        }

        /*
         * cannot semantically understand what's inside, so we leave it as suspected :0
         */

        List<String> result = new ArrayList<String>();
        result.addAll(simpleVariableResult);
        result.addAll(complexResult);
        if(result.size() > 0){
            return result;
        }

        return RESULT_DONT_KNOW;

    }

    /**
     * Tries to find declaration and usage of the variable. <br />
     *
     * @return RESULT_SAFE when I'm sure there is no XSS inside, RESULT_DONT_KNOW when I don't know, otherwise I suppose there might be a problem
     */
    public List<String> findVariableDeclaration(int lineNum, String vulnerableVariable, FileContent f, FileLoader loader) {
        if (!JAVA_VARIABLE_PATTERN.matcher(vulnerableVariable).matches()) {
            return RESULT_DONT_KNOW; // don't know if it's safe or not, all I know is this isn't variable :)
        }

        List<String> result = new ArrayList<String>();

        String lastVariableAssignment = null;
        int lastvariableAssignmentLineNum = 0;
        String declarationLine = null;
        int declarationLineLineNum = 0;

        Pattern safeDeclaration = Pattern.compile(SAFE_VARIABLE_DECLARATION_START + vulnerableVariable + " (=|:).*$");
        Pattern stringDeclaration = Pattern.compile("^.*<?(java.lang.)?String>?(\\[\\])? " + vulnerableVariable + " (=|:).*$");
        String variableAssignment = vulnerableVariable + " = ";

        // GO UP from current line to find:
        // 1, if variable is safe
        // 2, or declaration of the variable
        // 3, and the last assignment of the variable (to check if we can call the result safe)
        for (int i = lineNum - 1; i >= 0 && declarationLine == null; i--) {
            String fileLine = f.getContent().get(i).trim();
            // safe lines without declarations
            if (fileLine.startsWith("out.write") || fileLine.startsWith("out.print")) {
                continue;
            }
            if (safeDeclaration.matcher(fileLine).matches()) {
                return RESULT_SAFE;
            }
            if (stringDeclaration.matcher(fileLine).matches()) {
                declarationLine = fileLine;
                declarationLineLineNum = i;
            }
            if (lastVariableAssignment == null && !fileLine.matches("^\\s*//.*$") && fileLine.contains(variableAssignment)) {
                lastVariableAssignment = fileLine;
                lastvariableAssignmentLineNum = i;
            }
        }

        // check whether the last assignment modified the variable into a safe form
        if (declarationLine != null) {
            String assignmentContent = null;
            String assignmentLine = null;
            int assignmentLineNum = 0;

            if (lastVariableAssignment != null) {
                assignmentContent = lastVariableAssignment.substring(lastVariableAssignment.indexOf("=") + 1).trim();
                assignmentLineNum = lastvariableAssignmentLineNum;
                assignmentLine = lastVariableAssignment;
            } else {
                // for (a : b){
                if (declarationLine.contains(":")) {
                    int startIndex = declarationLine.indexOf(":");
                    int endIndex = declarationLine.indexOf(")", startIndex);
                    assignmentContent = declarationLine.substring(startIndex + 1, endIndex).trim();
                } else {
                    assignmentContent = declarationLine.substring(declarationLine.indexOf("=") + 1).trim();
                }
                assignmentLineNum = declarationLineLineNum;
                assignmentLine = declarationLine;
            }

            List<String> assignmentResult = isCallArgumentSuspected(assignmentContent, assignmentLineNum, assignmentLine, f, loader);
            if (assignmentResult == RESULT_SAFE) {
                // it's safe
                return RESULT_SAFE;
            }

            if (lastVariableAssignment != null && !declarationLine.equals(lastVariableAssignment)) {
                result.add(lastVariableAssignment);
            }
            result.add(declarationLine);
            result.addAll(assignmentResult);
            return result;
        }

        if (declarationLine != null) {
            result.add(declarationLine);
            return result;
        }

        return RESULT_DONT_KNOW; // can't decide
    }

    protected boolean isExpressionSafe(String expression) {
        // replace all \" from definitions (doesn't change the XSS meaning for the SAFE_XSS_RE)
        String normalizedFunctionArgument = expression.replaceAll(Matcher.quoteReplacement("\\\""), "");
        // try to filer out safe Portal API calls
        for (String safeCall : SAFE_API_CALLS) {
            if (normalizedFunctionArgument.startsWith(safeCall)) {
                return true;
            }
        }
        // try to filter out the safe expressions
        for (Pattern safeRE : SAFE_XSS_RE) {
            if (safeRE.matcher(normalizedFunctionArgument).matches()) {
                return true;
            }
        }

        return environment.getPortalAPICallsProcessor().isExpressionSafe(expression);
    }

    protected List<String> brakeComplexExpression(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        List<String> result = new ArrayList<String>();
        for(ComplexExpressionParser cep : complexExpressionParsers){
            List<String> callResult = cep.execute(expression, lineNum, line, f, loader);
            if(callResult == RESULT_SAFE){
                return RESULT_SAFE;
            }
            result.addAll(callResult);
        }
        return result;
    }

}
