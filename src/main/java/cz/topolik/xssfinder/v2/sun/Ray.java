package cz.topolik.xssfinder.v2.sun;

import cz.topolik.xssfinder.v2.animal.LadyBug;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.River;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class Ray implements Callable<Collection<LadyBug>> {
    private Tree tree;
    private AtomicInteger treesTotal;

    public static final String OUT_WRITE = "out.write(";
    public static final String OUT_PRINT = "out.print(";

    public static final String ROW_ADDTEXT = "row.addText(";
    static final Pattern ROW_URL_PATTERN = Pattern.compile("^(.*), (row[A-Z]+)$");

    public Ray(Tree tree, AtomicInteger treesTotal) {
        this.tree = tree;
        this.treesTotal = treesTotal;
    }

    @Override
    public Collection<LadyBug> call() {
        Set<LadyBug> result = new HashSet<LadyBug>();
        List<String> lines = tree.getRings();
        boolean insideComment = false;
        for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
            String line = lines.get(lineNum);

            if (line.startsWith("/*")) {
                insideComment = true;
            }
            if (insideComment) {
                if (line.endsWith("*/")) {
                    insideComment = false;
                }
                continue;
            }
            if (line.startsWith("//")) {
                continue;
            }

            Droplet droplet = new Droplet(null, lineNum, line, tree);

            String[] suspectedLineStacktrace = isLineSuspected(droplet);
            if (suspectedLineStacktrace != null) {
                result.add(new LadyBug(droplet, suspectedLineStacktrace));
            }
        }

        treesTotal.decrementAndGet();

        return result;
    }


    protected String[] isLineSuspected(Droplet droplet) {
        String line = droplet.getRing();

        // we have taglibs already processed
        // TODO: we have it processed but only to find vulnerable params, we don't save all threats
        if (World.see().snow().isTagLibJSP(droplet.getTree())) {
            return null;
        }

        // just string constants (Jasper convention)
        if (line.startsWith(OUT_WRITE)) {
            return null;
        }

        if (line.startsWith(OUT_PRINT)) {
            String argument = line.substring(OUT_PRINT.length(), line.length() - 2);
            List<String> result = World.see().river().isCallArgumentSuspected(droplet.droppy(argument));
            return result == River.TASTY ? null : result.toArray(new String[0]);
        }

        // vulnerable search container
        if (line.startsWith(ROW_ADDTEXT)) {
            String[] argument = parseSearchContainerRowExpression(droplet);
            List<String> result = World.see().river().isCallArgumentSuspected(droplet.droppy(argument[0]));
            if (argument.length > 1) {
                List<String> result1 = World.see().river().isCallArgumentSuspected(droplet.droppy(argument[1]));
                if (result == River.TASTY) {
                    result = result1;
                } else if (result1 != River.TASTY) {
                    result.addAll(result1);
                }
            }

            return result == River.TASTY ? null : result.toArray(new String[0]);
        }

        /*
        TODO: Search container Row Checker (and co.) + ResultRow attrs
         */

        // so we know there is no direct XSS
        // but there can be vulnerable taglib call

        String taglibResult[] = World.see().snow().isLineVulnerableTaglib(droplet);
        if (taglibResult == null) {
            // it's safe
            return null;
        }
        String argument = taglibResult[0].trim();
        List<String> callArgumentResult = World.see().river().isCallArgumentSuspected(droplet.droppy(argument));
        if (callArgumentResult == River.TASTY) {
            // it's safe
            return null;
        }
        String[] result = new String[taglibResult.length + callArgumentResult.size() - 1];
        System.arraycopy(callArgumentResult.toArray(new String[0]), 0, result, 0, callArgumentResult.size());
        System.arraycopy(taglibResult, 1, result, callArgumentResult.size(), taglibResult.length - 1);
        return result;
    }

    public static String[] parseSearchContainerRowExpression(Droplet droplet) {
        String argument = droplet.getRing();
        int i = droplet.getRingNum() + 1;
        while (!argument.endsWith(";")) {
            argument += droplet.getRing(i++);
        }

        argument = argument.substring(ROW_ADDTEXT.length(), argument.length() - 2);
        Matcher m = ROW_URL_PATTERN.matcher(argument);
        if (m.matches()) {
            return new String[]{m.group(1), m.group(2)};
        }
        return new String[]{argument};
    }
}
