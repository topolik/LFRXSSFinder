package cz.topolik.xssfinder.v2.animal.bug;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed JSP file into Java file
 *
 * @author Tomas Polesovsky
 */
public class LindenBurg extends BirchBug {
    public static final String OUT_WRITE = "out.write(";
    public static final String OUT_PRINT = "out.print(";
    public static final String ROW_ADDTEXT = "row.addText(";
    private static final Pattern ROW_URL_PATTERN = Pattern.compile("^(.*), (row[A-Z]+)$");
    private static final Pattern TAGLIB_CALL_PATTERN = Pattern.compile("^(_jspx_th_[\\w]+)\\.set([\\w]+)\\((.*)\\);$");

    public static String[] parseSearchContainerRowExpression(String growthRing, int year, Tree tree) {
        String completeRing = growthRing;
        while (completeRing.charAt(completeRing.length() - 1) != ';') {
            completeRing += tree.getRing(++year);
        }

        String ringContent = completeRing.substring(ROW_ADDTEXT.length(), completeRing.length() - 2);

        Matcher m = ROW_URL_PATTERN.matcher(ringContent);
        if (m.matches()) {
            return new String[]{m.group(1), m.group(2)};
        }

        return new String[]{ringContent};
    }

    @Override
    protected List<LadyBug> callLadyBugs(int yearOfBirth) {
        List<LadyBug> result = new ArrayList<LadyBug>(2);

        String ladies = tree.getRing(yearOfBirth);

        // just string constants (Jasper convention)
        if (tree.getRing(yearOfBirth).startsWith(OUT_WRITE)) {
            return result;
        }

        // direct output
        if (ladies.startsWith(OUT_PRINT)) {
            String dirtyLady = ladies.substring(OUT_PRINT.length(), ladies.length() - 2);
            cleanLadyBug(dirtyLady, yearOfBirth, result);
            return result;
        }

        // vulnerable search container
        if (ladies.startsWith(ROW_ADDTEXT)) {
            for (String dirtyLady : parseSearchContainerRowExpression(ladies, yearOfBirth, tree)) {
                cleanLadyBug(dirtyLady, yearOfBirth, result);
            }

            return result;
        }

        // taglib call
        Matcher m = TAGLIB_CALL_PATTERN.matcher(ladies);
        if (m.matches()) {
            String dirtyLady = parseTaglibExpression(m, yearOfBirth);
            if (dirtyLady != null) {
                cleanLadyBug(dirtyLady, yearOfBirth, result);
            }

            return result;
        }


        return result;
    }

    protected void cleanLadyBug(String dirtyLady, int yearOfBirth, List<LadyBug> cleanLadies) {
        LadyBug cleanLady = Droplet.surround(dirtyLady, yearOfBirth, tree).clean();
        if (!cleanLady.equals(LadyBug.NO_LADYBUG)) {
            cleanLadies.add(cleanLady);
        }
    }

    private String parseTaglibExpression(Matcher m, int yearOfBirth) {
        String taglibVariableName = m.group(1);
        String setFieldName = m.group(2);
        String argument = m.group(3);

        String taglibClassName = null;
        Pattern taglibDeclaration = Pattern.compile("^.*(com.liferay.taglib.[^ ]+) " + taglibVariableName + " (=|:).*$");
        for (int i = yearOfBirth; i >= 0 && taglibClassName == null; i--) {
            String fileLine = tree.getRing(i);
            Matcher declM = taglibDeclaration.matcher(fileLine);
            if (!declM.matches()) {
                continue;
            }
            taglibClassName = declM.group(1);
        }

        if (taglibClassName != null) {
            if (World.see().snow().isBlackSpot(taglibClassName, setFieldName)) {
                return argument;
            }
        }

        return null;
    }

}
