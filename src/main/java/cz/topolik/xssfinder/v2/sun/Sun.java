package cz.topolik.xssfinder.v2.sun;

import cz.topolik.xssfinder.PossibleXSSLine;
import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tomas Polesovsky
 */
public class Sun {
    private ExecutorService beams;
    private AtomicInteger treesToShineOn = new AtomicInteger();
    private long shineStartTime;

    public Sun(int beams) {
        Logger.log("Sun's going to have " + beams + " beams.");
        this.beams = Executors.newFixedThreadPool(beams);
    }

    public Set<PossibleXSSLine> shine()  {
        shineStartTime = System.currentTimeMillis();

        Logger.log("Let The Sunshine In ... @ " + DateFormat.getTimeInstance().format(new Date()));

        List<Future<Collection<PossibleXSSLine>>> enlightening = enlighten(World.see().forest().linden());

        waitForEnlightenment(enlightening);

        Logger.log("... was enlightened @ " + DateFormat.getTimeInstance().format(new Date()));

        return pickUpBeatles(enlightening);
    }

    private HashSet<PossibleXSSLine> pickUpBeatles(List<Future<Collection<PossibleXSSLine>>> enlightenment) {
        HashSet<PossibleXSSLine> beetles = new HashSet<PossibleXSSLine>();

        for (Future<Collection<PossibleXSSLine>> enlightedTree : enlightenment){
            try {
                beetles.addAll(enlightedTree.get());
            } catch (Exception e) {
                throw new SunShineException("Unable to pick up a beetle :(", e);
            }
        }

        return beetles;
    }

    private void waitForEnlightenment(List<Future<Collection<PossibleXSSLine>>> enlighting) {
        beams.shutdown();

        try {
            while (!beams.awaitTermination(10, TimeUnit.SECONDS)) {
                long shiningTime = (System.currentTimeMillis() - shineStartTime) / 1000;

                Logger.log("... after " + shiningTime +" secs there are still " + treesToShineOn.get() + " trees in the dark");
            }
        } catch (InterruptedException e) {
            throw new SunShineException("Too impatient to wait for enlightenment :(", e);
        }
    }


    private List<Future<Collection<PossibleXSSLine>>> enlighten(List<Tree> trees) {
        treesToShineOn.set(trees.size());

        List<Ray> rayTrees = new ArrayList<Ray>(trees.size());
        for (Tree tree : trees) {
            rayTrees.add(new Ray(tree, treesToShineOn));
        }

        try {
            return beams.invokeAll(rayTrees);
        } catch (InterruptedException e) {
            throw new SunShineException("Unable to enlighten all trees :( ", e);
        }
    }
}