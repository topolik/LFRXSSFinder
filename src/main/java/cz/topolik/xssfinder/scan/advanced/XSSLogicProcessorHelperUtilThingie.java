package cz.topolik.xssfinder.scan.advanced;

import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.scan.advanced.parser.*;
import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 *
 * @author Tomas Polesovsky
 */
public class XSSLogicProcessorHelperUtilThingie {
    public static List<String> RESULT_DONT_KNOW = new ArrayList<String>();
    public static List<String> RESULT_SAFE = new ArrayList<String>();

    List<ComplexExpressionParser> complexExpressionParsers = new ArrayList<ComplexExpressionParser>();
    List<WhitelistExpressionParser> whitelistExpressionParsers = new ArrayList<WhitelistExpressionParser>();
    List<String> SAFE_HASHES = new Vector<String>();

    static final String SAFE_VARIABLE_DECLARATION_START = ".*(byte|short|int|long|float|double|boolean|(java.lang.)?(Byte|Short|Integer|Long|Float|Double|Boolean))(\\[\\])? ";
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    static final String ESCAPED_QUOTE = Matcher.quoteReplacement("\\\"");
    private XSSEnvironment environment;

    public XSSLogicProcessorHelperUtilThingie(XSSEnvironment environment) {
        this.environment = environment;
        environment.setXSSLogicProcessorHelperUtilThingie(this);

        init();
    }

    public void destroy() {
        File safeHashesFile = new File(System.getProperty("java.io.tmpdir"), "LFRXSSFinder.safe-hashes.txt");
        if(!safeHashesFile.exists() || safeHashesFile.canWrite()){
            try {
                BufferedWriter sw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(safeHashesFile)));
                try {
                    TreeSet<String> sortedHashes = new TreeSet<String>(SAFE_HASHES);
                    for (String hash : sortedHashes) {
                        sw.write(hash.toString());
                        sw.newLine();
                    }
                } catch (IOException e){
                } finally {
                    if(sw != null){
                        try {
                            sw.close();
                        } catch (IOException ex) {}
                    }
                }
            } catch (FileNotFoundException e){}
        }
    }

    protected void init(){
        Logger.log("... loading built-in safe-expressions.txt");
        InputStream in = getClass().getResourceAsStream("/safe_expressions.txt");
        try {
            Scanner s = new Scanner(in);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if(line.startsWith("file=")){
                    String fileLine = line.substring(line.indexOf("=")+1);
                    int pos = fileLine.indexOf(",");
                    String fileName = fileLine.substring(0, pos);
                    String content = fileLine.substring(pos + 1);
                    // line num?
                    int lineNum = -1;
                    if (content.charAt(0) >= 48 && content.charAt(0) <= 57) {
                        pos = content.indexOf(",");
                        if (pos > 0) {
                            try {
                                lineNum = Integer.parseInt(content.substring(0, pos));
                                content = content.substring(pos + 1);

                                // indexes start from 0
                                lineNum--;
                            } catch (NumberFormatException e) {}
                        }
                    }
                    whitelistExpressionParsers.add(new FileContentCEP(fileName, content, lineNum));
                } else
                if(line.startsWith("simple=")){
                    String arg = line.substring(line.indexOf("=") + 1);
                    whitelistExpressionParsers.add(new SimplePrefixedWEP(arg));
                } else
                if(line.startsWith("pattern=")){
                    String arg = line.substring(line.indexOf("=")+1);
                    whitelistExpressionParsers.add(new RegExpWEP(arg));
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
                } else
                if(line.startsWith("reg-exp-replace-cep=")){
                    int replacementStart = line.indexOf("=")+1;
                    int replacementEnd = line.indexOf("=", replacementStart);
                    while(line.charAt(replacementEnd - 1) == '\\'){
                        replacementEnd = line.indexOf("=", replacementEnd);
                    }
                    String replacement = line.substring(replacementStart, replacementEnd);
                    int regExpStart = replacementEnd + 1;
                    String regExp = line.substring(regExpStart);
                    complexExpressionParsers.add(new RegExpReplaceCEP(regExp, replacement, environment));
                }
            }
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException ex) {}
            }
        }

        whitelistExpressionParsers.add(new EscapedModelWEP(environment));

        complexExpressionParsers.add(new ParenthesesCEP(environment));
        complexExpressionParsers.add(new BeanCallCEP(environment));
        complexExpressionParsers.add(new StringConcatCEP(environment));
        complexExpressionParsers.add(new JSPContextAttributeFinderCEP(environment));
        complexExpressionParsers.add(new StringBundlerCEP(environment));


        File safeHashesFile = new File(System.getProperty("java.io.tmpdir"), "LFRXSSFinder.safe-hashes.txt");
        Logger.log("... loading safe hashes from " + safeHashesFile);
        if(safeHashesFile.exists() && safeHashesFile.canRead()){
            try {
                in = new FileInputStream(safeHashesFile);
                try {
                    Scanner s = new Scanner(in);
                    while (s.hasNextLine()) {
                        SAFE_HASHES.add(s.nextLine());
                    }
                } finally {
                    if(in != null){
                        try {
                            in.close();
                        } catch (IOException ex) {}
                    }
                }
            } catch (FileNotFoundException e){}
        }
    }

    public List<String> isCallArgumentSuspected(String arg, int lineNum, String line, FileContent f, FileLoader loader) {
        String argument = arg.trim();
        while (argument.endsWith(";")) {
            argument = argument.substring(0, argument.length() - 1);
        }
        String nonQuotedArgument = argument.replaceAll(ESCAPED_QUOTE, "");

        String hash = f.getFile().getAbsolutePath().substring(loader.getPortalSourcesDir().getAbsolutePath().length()) + "," + lineNum + "," + argument;
        if(SAFE_HASHES.contains(hash)){
            return RESULT_SAFE;
        }

        if (isExpressionSimpleAndSafe(nonQuotedArgument, lineNum, line, f, loader)) {
            SAFE_HASHES.add(hash);
            return RESULT_SAFE;
        }

        // Try to break it using intelligent parsers
        List<String> complexResult = breakComplexExpression(nonQuotedArgument, lineNum, line, f, loader);
        if (complexResult == RESULT_SAFE) {
            // huh, it's safe
            SAFE_HASHES.add(hash);
            return RESULT_SAFE;
        }

        // OK, perhaps a variable assignment?
        List<String> simpleVariableResult = findVariableDeclaration(lineNum, nonQuotedArgument, f, loader);
        if (simpleVariableResult == RESULT_SAFE) {
            // it's safe :)
            SAFE_HASHES.add(hash);
            return RESULT_SAFE;
        }
        if(simpleVariableResult != RESULT_DONT_KNOW){
            // OK, it was a variable we don't need to continue
            return simpleVariableResult;
        }

        /*
         * cannot semantically understand what's inside, so we leave it as a suspected :O
         */

        List<String> result = new ArrayList<String>();
        result.addAll(simpleVariableResult);
        result.addAll(complexResult);
        if(result.size() > 0){
            return result;
        }

        return RESULT_DONT_KNOW;

    }

    public Pattern buildVariableDeclaration(String variable) {
        return Pattern.compile("^.*(private |public |protected |static |final |volatile )*([a-z\\.]+\\.)?[A-Z][\\w0-9]+(<.+>)?(\\[\\])? " + variable + " ?(=|:).*$");
    }

    /**
     * Tries to find declaration and usage of the variable. <br />
     *
     * @return RESULT_SAFE when I'm sure there is no XSS inside, RESULT_DONT_KNOW when I don't know, otherwise I suppose there might be a problem
     */
    public List<String> findVariableDeclaration(int lineNum, String vulnerableVariable, FileContent f, FileLoader loader) {
        if (vulnerableVariable.trim().length() == 0) {
            return RESULT_DONT_KNOW;
        }

        if (!JAVA_VARIABLE_PATTERN.matcher(vulnerableVariable).matches()) {
            return RESULT_DONT_KNOW; // don't know if it's safe or not. All I know is that it isn't valid variable :)
        }

        List<String> result = new ArrayList<String>();

        String lastVariableAssignment = null;
        int lastvariableAssignmentLineNum = 0;
        String declarationLine = null;
        int declarationLineLineNum = 0;

        Pattern safeDeclaration = Pattern.compile(SAFE_VARIABLE_DECLARATION_START + vulnerableVariable + " ?(=|:).*$");
        Pattern variableDeclaration = buildVariableDeclaration(vulnerableVariable);
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
            if (variableDeclaration.matcher(fileLine).matches()) {
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
                // for (a : b) {
                if (declarationLine.contains(":")) {
                    int startIndex = declarationLine.indexOf(":");
                    int endIndex = declarationLine.lastIndexOf(")");
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

        return RESULT_DONT_KNOW; // unable to decide
    }

    protected boolean isExpressionSimpleAndSafe(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
        for (WhitelistExpressionParser wep : whitelistExpressionParsers) {
            if (wep.isSafe(expression, lineNum, line, f, loader)) {
                return true;
            }
        }

        return environment.getPortalAPICallsProcessor().isExpressionSafe(expression);
    }

    protected List<String> breakComplexExpression(String expression, int lineNum, String line, FileContent f, FileLoader loader) {
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
