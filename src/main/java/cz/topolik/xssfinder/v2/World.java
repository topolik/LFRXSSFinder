package cz.topolik.xssfinder.v2;

import cz.topolik.xssfinder.PossibleXSSLine;
import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Rain;
import cz.topolik.xssfinder.v2.water.River;
import cz.topolik.xssfinder.v2.water.Snow;
import cz.topolik.xssfinder.v2.wood.Forest;
import cz.topolik.xssfinder.v2.wood.Tree;
import cz.topolik.xssfinder.v2.wood.Wind;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class World {
    private Forest forest;
    private Snow snow = new Snow();
    private Rain rain = new Rain();
    private River river = new River();
    private Wind wind = new Wind();
    private static final World world = new World();

    public static World see() {
        return world;
    }

    private World() {
        // only God can create World
    }

    public void explore(File... continents) {
        forest = wind.explore(continents);

        forest.callAntsToExplore();

        rain.fallDown();
        snow.fly();
        river.flow();
    }

    public Set<PossibleXSSLine> rotate(int speed) {
        return new SunLight(speed).scan();
    }

    public void jDay() {
        river.dryUp();
    }

    public Forest forest() {
        return forest;
    }

    public Rain rain() {
        return rain;
    }

    public River river() {
        return river;
    }

    public Snow snow() {
        return snow;
    }
}

class SunLight {
    private static final Set<PossibleXSSLine> EMPTY_RESPONSE = new HashSet<PossibleXSSLine>();
    private ExecutorService pool;
    private Vector<Set<PossibleXSSLine>> scanResults = new Vector<Set<PossibleXSSLine>>();
    private Vector<String> remainingFilesToProcess = new Vector<String>();

    static final String OUT_WRITE = "out.write(";
    public static final String ROW_ADDTEXT = "row.addText(";
    static final Pattern ROW_URL_PATTERN = Pattern.compile("^(.*), (row[A-Z]+)$");

    static final String OUT_PRINT = "out.print(";

    public SunLight(int poolSize) {
        Logger.log("Initializing ThreadedXSSScanner with pool size: " + poolSize);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public Set<PossibleXSSLine> scan() {
        {
            Logger.log("Running advanced scan");
            {
                Logger.log("Starting scanning ...");
                int pos = 0;
                List<Tree> trees = World.see().forest().linden();
                for (Tree tree : trees) {
                    pos++;
                    if (pos % 10 == 0) {
                        Logger.log("Scanned " + pos + " of " + trees.size() + " files");
                    }
                    scan(tree);
                }
                Logger.log("Finished");

            }
        }
        long time = System.currentTimeMillis();
        pool.shutdown();
        try {
            Logger.log("Waiting for threads to terminate... 10s");
            while (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                if (remainingFilesToProcess.size() < 10) {
                    StringBuffer sb = new StringBuffer();
                    for (String file : remainingFilesToProcess) {
                        sb.append("......");
                        sb.append(file);
                        sb.append("\n");
                    }
                    Logger.log(sb.toString());
                }

                long soFar = (System.currentTimeMillis() - time) / 1000;
                Logger.log("... remaining files: " + remainingFilesToProcess.size() + "... waiting another 10s, total time: " + soFar + "s");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Logger.log("... not all threads finished successfully!");
        }
        Logger.log("... finished");

        HashSet<PossibleXSSLine> result = new HashSet<PossibleXSSLine>();
        for (Set<PossibleXSSLine> lines : scanResults) {
            result.addAll(lines);
        }
        return result;
    }

    protected Set<PossibleXSSLine> scan(final Tree tree) {
        remainingFilesToProcess.add(tree.getName());
        pool.execute(new Runnable() {
            @Override
            public void run() {
                Set<PossibleXSSLine> result = SunLight.this.scan2(tree);
                SunLight.this.scanResults.add(result);
                remainingFilesToProcess.remove(tree.getName());
            }
        });

        return EMPTY_RESPONSE;
    }

    protected Set<PossibleXSSLine> scan2(Tree tree) {
        Set<PossibleXSSLine> result = new HashSet<PossibleXSSLine>();
        List<String> lines = tree.getGrowthRings();
        boolean insideComment = false;
        for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
            String line = lines.get(lineNum).trim();

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
                result.add(new PossibleXSSLine(droplet, suspectedLineStacktrace));
            }
        }
        return result;
    }

    protected String[] isLineSuspected(Droplet droplet) {
        String line = droplet.getGrowthRing();

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
            return result == River.RESULT_SAFE ? null : result.toArray(new String[0]);
        }

        // vulnerable search container
        if (line.startsWith(ROW_ADDTEXT)) {
            String[] argument = parseSearchContainerRowExpression(droplet);
            List<String> result = World.see().river().isCallArgumentSuspected(droplet.droppy(argument[0]));
            if (argument.length > 1) {
                List<String> result1 = World.see().river().isCallArgumentSuspected(droplet.droppy(argument[1]));
                if (result == River.RESULT_SAFE) {
                    result = result1;
                } else if (result1 != River.RESULT_SAFE) {
                    result.addAll(result1);
                }
            }

            return result == River.RESULT_SAFE ? null : result.toArray(new String[0]);
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
        if (callArgumentResult == River.RESULT_SAFE) {
            // it's safe
            return null;
        }
        String[] result = new String[taglibResult.length + callArgumentResult.size() - 1];
        System.arraycopy(callArgumentResult.toArray(new String[0]), 0, result, 0, callArgumentResult.size());
        System.arraycopy(taglibResult, 1, result, callArgumentResult.size(), taglibResult.length - 1);
        return result;
    }

    public static String[] parseSearchContainerRowExpression(Droplet droplet) {
        String argument = droplet.getGrowthRing().trim();
        int i = droplet.getGrowthRingNum() + 1;
        while (!argument.endsWith(";")) {
            argument += droplet.getTree().getGrowthRings().get(i++).trim();
        }

        argument = argument.substring(ROW_ADDTEXT.length(), argument.length() - 2);
        Matcher m = ROW_URL_PATTERN.matcher(argument);
        if (m.matches()) {
            return new String[]{m.group(1), m.group(2)};
        }
        return new String[]{argument};
    }
}