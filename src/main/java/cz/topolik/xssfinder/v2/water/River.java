package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.butterfly.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class River {
    public static List<String> RESULT_DONT_KNOW = new ArrayList<String>();
    public static List<String> RESULT_SAFE = new ArrayList<String>();

    List<ColoredButterfly> coloredButterflies = new ArrayList<ColoredButterfly>();
    List<WhiteButterfly> whiteButterflies = new ArrayList<WhiteButterfly>();
    List<String> SAFE_HASHES = new Vector<String>();

    static final String SAFE_VARIABLE_DECLARATION_START = ".*(byte|short|int|long|float|double|boolean|(java.lang.)?(Byte|Short|Integer|Long|Float|Double|Boolean))(\\[\\])? ";
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    static final String ESCAPED_QUOTE = Matcher.quoteReplacement("\\\"");

    public void dryUp() {
        File safeHashesFile = new File(System.getProperty("java.io.tmpdir"), "LFRXSSFinder.safe-hashes.txt");
        if (!safeHashesFile.exists() || safeHashesFile.canWrite()) {
            try {
                BufferedWriter sw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(safeHashesFile)));
                try {
                    TreeSet<String> sortedHashes = new TreeSet<String>(SAFE_HASHES);
                    for (String hash : sortedHashes) {
                        sw.write(hash.toString());
                        sw.newLine();
                    }
                } catch (IOException e) {
                } finally {
                    if (sw != null) {
                        try {
                            sw.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            } catch (FileNotFoundException e) {
            }
        }
    }

    public void flow() {
        Logger.log("... loading built-in safe-expressions.txt");
        InputStream in = getClass().getResourceAsStream("/safe_expressions.txt");
        try {
            Scanner s = new Scanner(in);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (line.startsWith("file=")) {
                    String fileLine = line.substring(line.indexOf("=") + 1);
                    int pos = fileLine.indexOf(",");
                    String fileName = fileLine.substring(0, pos);
                    String content = fileLine.substring(pos + 1);
                    // line num?
                    int growthRingNum = -1;
                    if (content.charAt(0) >= 48 && content.charAt(0) <= 57) {
                        pos = content.indexOf(",");
                        if (pos > 0) {
                            try {
                                growthRingNum = Integer.parseInt(content.substring(0, pos));
                                content = content.substring(pos + 1);

                                // indexes start from 0
                                growthRingNum--;
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                    whiteButterflies.add(new TreeWhiteButterfly(fileName, content, growthRingNum));
                } else if (line.startsWith("simple=")) {
                    String arg = line.substring(line.indexOf("=") + 1);
                    whiteButterflies.add(new SimpleWhiteButtefly(arg));
                } else if (line.startsWith("pattern=")) {
                    String arg = line.substring(line.indexOf("=") + 1);
                    whiteButterflies.add(new PatternWhiteButtefly(arg));
                } else if (line.startsWith("reg-exp-cep=")) {
                    int groupsStart = line.indexOf("=") + 1;
                    int groupsEnd = line.indexOf("=", groupsStart);
                    int reStart = groupsEnd + 1;
                    String[] groupsStr = line.substring(groupsStart, groupsEnd).split(",");
                    String regExp = line.substring(reStart);
                    int[] groups = new int[groupsStr.length];
                    for (int i = 0; i < groupsStr.length; i++) {
                        groups[i] = Integer.parseInt(groupsStr[i]);
                    }
                    coloredButterflies.add(new REColoredButterfly(regExp, groups));
                } else if (line.startsWith("reg-exp-replace-cep=")) {
                    int replacementStart = line.indexOf("=") + 1;
                    int replacementEnd = line.indexOf("=", replacementStart);
                    while (line.charAt(replacementEnd - 1) == '\\') {
                        replacementEnd = line.indexOf("=", replacementEnd);
                    }
                    String replacement = line.substring(replacementStart, replacementEnd);
                    int regExpStart = replacementEnd + 1;
                    String regExp = line.substring(regExpStart);
                    coloredButterflies.add(new REReplaceColoredButtefly(regExp, replacement));
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }

        whiteButterflies.add(new EscapeWhiteButterfly());

        coloredButterflies.add(new BigRareColoredButterfly());
        coloredButterflies.add(new BeanColoredButterfly());
        coloredButterflies.add(new StringGlueColoredButterfly());
        coloredButterflies.add(new JSPContextAttributeFinderColoredButterfly());
        coloredButterflies.add(new SBColoredButterfly());


        File safeHashesFile = new File(System.getProperty("java.io.tmpdir"), "LFRXSSFinder.safe-hashes.txt");
        Logger.log("... loading safe hashes from " + safeHashesFile);
        if (safeHashesFile.exists() && safeHashesFile.canRead()) {
            try {
                in = new FileInputStream(safeHashesFile);
                try {
                    Scanner s = new Scanner(in);
                    while (s.hasNextLine()) {
                        SAFE_HASHES.add(s.nextLine());
                    }
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            } catch (FileNotFoundException e) {
            }
        }
    }

    public List<String> isCallArgumentSuspected(Droplet droplet) {
        String argument = droplet.getExpression().trim();
        while (argument.endsWith(";")) {
            argument = argument.substring(0, argument.length() - 1);
        }
        String nonQuotedArgument = argument.replaceAll(ESCAPED_QUOTE, "");

        String hash = droplet.getTree().getRoot().getAbsolutePath().substring(droplet.getTree().getContinent().getAbsolutePath().length()) + "," + droplet.getGrowthRingNum() + "," + argument;
        if (SAFE_HASHES.contains(hash)) {
            return RESULT_SAFE;
        }

        if (isExpressionSimpleAndSafe(droplet.droppy(nonQuotedArgument))) {
            SAFE_HASHES.add(hash);
            return RESULT_SAFE;
        }

        // Try to break it using intelligent parsers
        List<String> complexResult = breakComplexExpression(droplet.droppy(nonQuotedArgument));
        if (complexResult == RESULT_SAFE) {
            // huh, it's safe
            SAFE_HASHES.add(hash);
            return RESULT_SAFE;
        }

        // OK, perhaps a variable assignment?
        List<String> simpleVariableResult = findVariableDeclaration(droplet.droppy(nonQuotedArgument));
        if (simpleVariableResult == RESULT_SAFE) {
            // it's safe :)
            SAFE_HASHES.add(hash);
            return RESULT_SAFE;
        }
        if (simpleVariableResult != RESULT_DONT_KNOW) {
            // OK, it was a variable we don't need to continue
            return simpleVariableResult;
        }

        /*
         * cannot semantically understand what's inside, so we leave it as a suspected :O
         */

        List<String> result = new ArrayList<String>();
        result.addAll(simpleVariableResult);
        result.addAll(complexResult);
        if (result.size() > 0) {
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
    public List<String> findVariableDeclaration(Droplet droplet) {
        String vulnerableVariable = droplet.getExpression();

        if (vulnerableVariable.trim().length() == 0) {
            return RESULT_DONT_KNOW;
        }

        if (!JAVA_VARIABLE_PATTERN.matcher(vulnerableVariable).matches()) {
            return RESULT_DONT_KNOW; // don't know if it's safe or not. All I know is that it isn't valid variable :)
        }

        List<String> result = new ArrayList<String>();

        String lastVariableAssignment = null;
        int lastvariableAssignmentgrowthRingNum = 0;
        String declarationLine = null;
        int declarationLinegrowthRingNum = 0;

        Pattern safeDeclaration = Pattern.compile(SAFE_VARIABLE_DECLARATION_START + vulnerableVariable + " ?(=|:).*$");
        Pattern variableDeclaration = buildVariableDeclaration(vulnerableVariable);
        String variableAssignment = vulnerableVariable + " = ";

        // GO UP from current line to find:
        // 1, if variable is safe
        // 2, or declaration of the variable
        // 3, and the last assignment of the variable (to check if we can call the result safe)
        for (int i = droplet.getGrowthRingNum() - 1; i >= 0 && declarationLine == null; i--) {
            String fileLine = droplet.getTree().getGrowthRings().get(i).trim();
            // safe lines without declarations
            if (fileLine.startsWith("out.write") || fileLine.startsWith("out.print")) {
                continue;
            }
            if (safeDeclaration.matcher(fileLine).matches()) {
                return RESULT_SAFE;
            }
            if (variableDeclaration.matcher(fileLine).matches()) {
                declarationLine = fileLine;
                declarationLinegrowthRingNum = i;
            }
            if (lastVariableAssignment == null && !fileLine.matches("^\\s*//.*$") && fileLine.contains(variableAssignment)) {
                lastVariableAssignment = fileLine;
                lastvariableAssignmentgrowthRingNum = i;
            }
        }

        // check whether the last assignment modified the variable into a safe form
        if (declarationLine != null) {
            String assignmentContent = null;
            String assignmentGrowthRing = null;
            int assignmentGrowthRingNum = 0;

            if (lastVariableAssignment != null) {
                assignmentContent = lastVariableAssignment.substring(lastVariableAssignment.indexOf("=") + 1).trim();
                assignmentGrowthRingNum = lastvariableAssignmentgrowthRingNum;
                assignmentGrowthRing = lastVariableAssignment;
            } else {
                // for (a : b) {
                if (declarationLine.contains(":")) {
                    int startIndex = declarationLine.indexOf(":");
                    int endIndex = declarationLine.lastIndexOf(")");
                    assignmentContent = declarationLine.substring(startIndex + 1, endIndex).trim();
                } else {
                    assignmentContent = declarationLine.substring(declarationLine.indexOf("=") + 1).trim();
                }
                assignmentGrowthRingNum = declarationLinegrowthRingNum;
                assignmentGrowthRing = declarationLine;
            }

            List<String> assignmentResult = isCallArgumentSuspected(droplet.droppy(assignmentContent, assignmentGrowthRingNum, assignmentGrowthRing));
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

    protected boolean isExpressionSimpleAndSafe(Droplet droplet) {
        for (WhiteButterfly butterfly : whiteButterflies) {
            if (butterfly.isSafe(droplet)) {
                return true;
            }
        }

        return World.see().rain().isExpressionSafe(droplet.getExpression());
    }

    protected List<String> breakComplexExpression(Droplet droplet) {
        List<String> result = new ArrayList<String>();
        for (ColoredButterfly butterfly : coloredButterflies) {
            List<String> callResult = butterfly.execute(droplet);
            if (callResult == RESULT_SAFE) {
                return RESULT_SAFE;
            }
            result.addAll(callResult);
        }
        return result;
    }

}
