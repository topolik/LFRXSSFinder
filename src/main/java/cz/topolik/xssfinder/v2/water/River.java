package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.animal.fish.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class River {
    public static final List<String> UNEATABLE = new ArrayList<String>();
    public static final List<String> TASTY = new ArrayList<String>();
    private static final File FISH_MEMORY = new File(System.getProperty("java.io.tmpdir"), "LFRXSSFinder.fish.memory.txt");
    private static final String FISH_EXPERIENCE = "/fish_experience.txt";

    List<RainbowFish> rainbowFishes = new ArrayList<RainbowFish>();
    List<WhiteFish> whiteFishes = new ArrayList<WhiteFish>();
    List<String> SAFE_MEMORIES = new Vector<String>();

    static final String SAFE_VARIABLE_DECLARATION_START = ".*(byte|short|int|long|float|double|boolean|(java.lang.)?(Byte|Short|Integer|Long|Float|Double|Boolean))(\\[\\])? ";
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    static final String ESCAPED_QUOTE = Matcher.quoteReplacement("\\\"");

    public void flow() {
        World.announce("Let's river flow ...");
        World.announce(" ... training fishes from " + FISH_EXPERIENCE);
        trainFishes();

        World.announce(" ... recovering fishes' memory from " + FISH_MEMORY);
        recoverFishesMemory();
    }


    public void dryUp() {
        World.announce("Saving fishes' memory to " + FISH_MEMORY);

        if (!FISH_MEMORY.exists() || FISH_MEMORY.canWrite()) {
            try {
                BufferedWriter sw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FISH_MEMORY)));
                try {
                    TreeSet<String> sortedHashes = new TreeSet<String>(SAFE_MEMORIES);
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

    protected void trainFishes(){
        InputStream in = getClass().getResourceAsStream(FISH_EXPERIENCE);
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
                    whiteFishes.add(new TreeWhiteFish(fileName, content, growthRingNum));
                } else if (line.startsWith("simple=")) {
                    String arg = line.substring(line.indexOf("=") + 1);
                    whiteFishes.add(new SimpleWhiteFish(arg));
                } else if (line.startsWith("pattern=")) {
                    String arg = line.substring(line.indexOf("=") + 1);
                    whiteFishes.add(new PatternWhiteFish(arg));
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
                    rainbowFishes.add(new RERainbowFish(regExp, groups));
                } else if (line.startsWith("reg-exp-replace-cep=")) {
                    int replacementStart = line.indexOf("=") + 1;
                    int replacementEnd = line.indexOf("=", replacementStart);
                    while (line.charAt(replacementEnd - 1) == '\\') {
                        replacementEnd = line.indexOf("=", replacementEnd);
                    }
                    String replacement = line.substring(replacementStart, replacementEnd);
                    int regExpStart = replacementEnd + 1;
                    String regExp = line.substring(regExpStart);
                    rainbowFishes.add(new REReplaceRainbowFish(regExp, replacement));
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

        whiteFishes.add(new EscapeWhiteFish());

        rainbowFishes.add(new BigRareRainbowFish());
        rainbowFishes.add(new BeanRainbowFish());
        rainbowFishes.add(new StringGlueRainbowFish());
        rainbowFishes.add(new JSPContextAttributeFinderRainbowFish());
        rainbowFishes.add(new SBRainbowFish());
    }

    protected void recoverFishesMemory() {
        if (FISH_MEMORY.exists() && FISH_MEMORY.canRead()) {
            try {
                InputStream in = new FileInputStream(FISH_MEMORY);
                try {
                    Scanner s = new Scanner(in);
                    while (s.hasNextLine()) {
                        SAFE_MEMORIES.add(s.nextLine());
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

        String memory = droplet.getTreeRoot().getAbsolutePath().substring(droplet.getTree().getContinent().getAbsolutePath().length()) + "," + droplet.getRingNum() + "," + argument;
        if (SAFE_MEMORIES.contains(memory)) {
            return TASTY;
        }

        if (isExpressionSimpleAndSafe(droplet.droppy(nonQuotedArgument))) {
            SAFE_MEMORIES.add(memory);
            return TASTY;
        }

        // Try to break it using intelligent parsers
        List<String> complexResult = breakComplexExpression(droplet.droppy(nonQuotedArgument));
        if (complexResult == TASTY) {
            // huh, it's safe
            SAFE_MEMORIES.add(memory);
            return TASTY;
        }

        // OK, perhaps a variable assignment?
        List<String> simpleVariableResult = findVariableDeclaration(droplet.droppy(nonQuotedArgument));
        if (simpleVariableResult == TASTY) {
            // it's safe :)
            SAFE_MEMORIES.add(memory);
            return TASTY;
        }
        if (simpleVariableResult != UNEATABLE) {
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

        return UNEATABLE;

    }

    public Pattern buildVariableDeclaration(String variable) {
        return Pattern.compile("^.*(private |public |protected |static |final |volatile )*([a-z\\.]+\\.)?[A-Z][\\w0-9]+(<.+>)?(\\[\\])? " + variable + " ?(=|:).*$");
    }

    /**
     * Tries to find declaration and usage of the variable. <br />
     *
     * @return TASTY when I'm sure there is no XSS inside, UNEATABLE when I don't know, otherwise I suppose there might be a problem
     */
    public List<String> findVariableDeclaration(Droplet droplet) {
        String vulnerableVariable = droplet.getExpression();

        if (vulnerableVariable.trim().length() == 0) {
            return UNEATABLE;
        }

        if (!JAVA_VARIABLE_PATTERN.matcher(vulnerableVariable).matches()) {
            return UNEATABLE; // don't know if it's safe or not. All I know is that it isn't valid variable :)
        }

        List<String> result = new ArrayList<String>();

        String lastVariableAssignment = null;
        int lastVariableAssignmentRingNum = 0;
        String declaration = null;
        int declarationRingNum = 0;

        Pattern safeDeclaration = Pattern.compile(SAFE_VARIABLE_DECLARATION_START + vulnerableVariable + " ?(=|:).*$");
        Pattern variableDeclaration = buildVariableDeclaration(vulnerableVariable);
        String variableAssignment = vulnerableVariable + " = ";

        // GO UP from current line to find:
        // 1, if variable is safe
        // 2, or declaration of the variable
        // 3, and the last assignment of the variable (to check if we can call the result safe)
        for (int i = droplet.getRingNum() - 1; i >= 0 && declaration == null; i--) {
            String fileLine = droplet.getRing(i);
            // safe lines without declarations
            if (fileLine.startsWith("out.write") || fileLine.startsWith("out.print")) {
                continue;
            }
            if (safeDeclaration.matcher(fileLine).matches()) {
                return TASTY;
            }
            if (variableDeclaration.matcher(fileLine).matches()) {
                declaration = fileLine;
                declarationRingNum = i;
            }
            if (lastVariableAssignment == null && !fileLine.matches("^\\s*//.*$") && fileLine.contains(variableAssignment)) {
                lastVariableAssignment = fileLine;
                lastVariableAssignmentRingNum = i;
            }
        }

        // check whether the last assignment modified the variable into a safe form
        if (declaration != null) {
            String assignmentContent = null;
            String assignmentGrowthRing = null;
            int assignmentGrowthRingNum = 0;

            if (lastVariableAssignment != null) {
                assignmentContent = lastVariableAssignment.substring(lastVariableAssignment.indexOf("=") + 1).trim();
                assignmentGrowthRingNum = lastVariableAssignmentRingNum;
                assignmentGrowthRing = lastVariableAssignment;
            } else {
                // for (a : b) {
                if (declaration.contains(":")) {
                    int startIndex = declaration.indexOf(":");
                    int endIndex = declaration.lastIndexOf(")");
                    assignmentContent = declaration.substring(startIndex + 1, endIndex).trim();
                } else {
                    assignmentContent = declaration.substring(declaration.indexOf("=") + 1).trim();
                }
                assignmentGrowthRingNum = declarationRingNum;
                assignmentGrowthRing = declaration;
            }

            List<String> assignmentResult = isCallArgumentSuspected(droplet.droppy(assignmentContent, assignmentGrowthRingNum, assignmentGrowthRing));
            if (assignmentResult == TASTY) {
                // it's safe
                return TASTY;
            }

            if (lastVariableAssignment != null && !declaration.equals(lastVariableAssignment)) {
                result.add(lastVariableAssignment);
            }
            result.add(declaration);
            result.addAll(assignmentResult);
            return result;
        }

        if (declaration != null) {
            result.add(declaration);
            return result;
        }

        return UNEATABLE; // unable to decide
    }

    protected boolean isExpressionSimpleAndSafe(Droplet droplet) {
        for (WhiteFish fish : whiteFishes) {
            if (fish.likes(droplet)) {
                return true;
            }
        }

        return World.see().rain().isExpressionSafe(droplet.getExpression());
    }

    protected List<String> breakComplexExpression(Droplet droplet) {
        List<String> result = new ArrayList<String>();
        for (RainbowFish fish : rainbowFishes) {
            List<String> callResult = fish.swallow(droplet);
            if (callResult == TASTY) {
                return TASTY;
            }
            result.addAll(callResult);
        }
        return result;
    }

}
