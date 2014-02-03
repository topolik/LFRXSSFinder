package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.animal.fish.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class River {
    static final String SAFE_VARIABLE_DECLARATION_START = ".*(byte|short|int|long|float|double|boolean|(java.lang.)?(Byte|Short|Integer|Long|Float|Double|Boolean))(\\[\\])? ";
    static final Pattern JAVA_VARIABLE_PATTERN = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
    static final String ESCAPED_QUOTE = Matcher.quoteReplacement("\\\"");
    private static final String FISH_EXPERIENCE = "/fish_experience.txt";
    List<RainbowFish> rainbowFishes = new ArrayList<RainbowFish>();
    List<WhiteFish> whiteFishes = new ArrayList<WhiteFish>();

    public void flow() {
        World.announce("Let's river flow ...");
        World.announce(" ... training fishes from " + FISH_EXPERIENCE);
        trainFishes();
    }

    public void dryUp() {
        List<UsefulFish> usefulFishes = new ArrayList<UsefulFish>(rainbowFishes.size() + whiteFishes.size());
        for (RainbowFish fish : rainbowFishes) {
            if (fish instanceof UsefulFish) {
                usefulFishes.add((UsefulFish) fish);
            }
        }
        for (WhiteFish fish : whiteFishes) {
            if (fish instanceof UsefulFish) {
                usefulFishes.add((UsefulFish) fish);
            }
        }

        World.announce("Looking for fishes that were not useful ...");
        for (UsefulFish fish : usefulFishes) {
            if (!fish.isUseful()) {
                World.announce(" ... " + fish);
            }
        }
    }

    protected void trainFishes() {
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

        whiteFishes.add(new SnowWhiteFish());
        whiteFishes.add(new EscapeWhiteFish());

        rainbowFishes.add(new BigRareRainbowFish());
        rainbowFishes.add(new BeanRainbowFish());
        rainbowFishes.add(new StringGlueRainbowFish());
        rainbowFishes.add(new JSPContextAttributeFinderRainbowFish());
        rainbowFishes.add(new SBRainbowFish());
    }

    public Water isEdible(Droplet droplet) {
        String argument = droplet.getExpression().trim();
        while (argument.endsWith(";")) {
            argument = argument.substring(0, argument.length() - 1);
        }

        Droplet unquotedDroplet = droplet.droppy(argument.replaceAll(ESCAPED_QUOTE, ""));

        if (doWhiteFishesLikeIt(unquotedDroplet)) {
            return Water.CLEAN_WATER;
        }

        Water complexResult = rainbowFishesOpinion(unquotedDroplet);
        if (complexResult.equals(Water.CLEAN_WATER)) {
            return Water.CLEAN_WATER;
        }

        // OK, perhaps a variable assignment?
        Water simpleVariableResult = findVariableDeclaration(unquotedDroplet);
        if (simpleVariableResult.equals(Water.CLEAN_WATER)) {
            return Water.CLEAN_WATER;
        }

        /*
         * cannot semantically understand what's inside, so we leave it as a suspected :O
         */

        Water result = new Water();
        result.add(simpleVariableResult);
        result.add(complexResult);
        return result;
    }

    public Pattern buildVariableDeclaration(String variable) {
        return Pattern.compile("^.*(private |public |protected |static |final |volatile )*([a-z\\.]+\\.)?[A-Z][\\w0-9]+(<.+>)?(\\[\\])? " + variable + " ?(=|:).*$");
    }

    /**
     * Tries to find declaration and usage of the variable. <br />
     *
     * @return {@see Water.CLEAN_WATER} when I'm sure there is no XSS inside, {@see Water.DIRTY_UNKNOWN_WATER} when I don't know, otherwise I suppose there might be a problem
     */
    public Water findVariableDeclaration(Droplet droplet) {
        String vulnerableVariable = droplet.getExpression();

        if (vulnerableVariable.trim().length() == 0) {
            return Water.UNKNOWN_WATER;
        }

        if (!JAVA_VARIABLE_PATTERN.matcher(vulnerableVariable).matches()) {
            return Water.UNKNOWN_WATER; // don't know if it's safe or not. All I know is that it isn't valid variable :)
        }

        Water result = new Water();

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
                return Water.CLEAN_WATER;
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

            Water assignmentResult = droplet.droppy(assignmentContent, assignmentGrowthRingNum, assignmentGrowthRing).dryUp();
            if (assignmentResult.equals(Water.CLEAN_WATER)) {
                // it's safe
                return Water.CLEAN_WATER;
            }

            if (lastVariableAssignment != null && !declaration.equals(lastVariableAssignment)) {
                result.add(lastVariableAssignment);
            }
            result.add(declaration);
            result.add(assignmentResult);
            return result;
        }

        if (declaration != null) {
            result.add(declaration);
        }

        return result;
    }

    protected boolean doWhiteFishesLikeIt(Droplet droplet) {
        for (WhiteFish fish : whiteFishes) {
            if (fish.likes(droplet)) {
                if (fish instanceof UsefulFish) {
                    ((UsefulFish) fish).setUseful(true);
                }
                return true;
            }
        }

        return false;
    }

    protected Water rainbowFishesOpinion(Droplet droplet) {
        Water result = new Water();
        for (RainbowFish fish : rainbowFishes) {
            Water callResult = fish.swallow(droplet);
            if (callResult.equals(Water.CLEAN_WATER)) {
                if (fish instanceof UsefulFish) {
                    ((UsefulFish) fish).setUseful(true);
                }
                return Water.CLEAN_WATER;
            }
            result.add(callResult);
        }
        return result;
    }

}