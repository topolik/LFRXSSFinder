package cz.topolik.xssfinder.v2.sun;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.animal.bug.LadyBug;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tomas Polesovsky
 */
public class Sun {
    private ExecutorService beams;
    private AtomicInteger treesToShineOn = new AtomicInteger();
    private long shineStartTime;

    public Sun(int beams) {
        World.announce("Sun is going to have " + beams + " beams.");
        this.beams = Executors.newFixedThreadPool(beams);
    }

    public Set<LadyBug> shine()  {
        shineStartTime = System.currentTimeMillis();

        World.announce("Let The Sunshine In ... @ " + DateFormat.getTimeInstance().format(new Date()));

        List<Future<Collection<LadyBug>>> enlightening = enlighten(World.see().forest().linden());

        waitForEnlightenment(enlightening);

        World.announce("... was enlightened @ " + DateFormat.getTimeInstance().format(new Date()));

        return pickUpBeatles(enlightening);
    }

    private HashSet<LadyBug> pickUpBeatles(List<Future<Collection<LadyBug>>> enlightenment) {
        HashSet<LadyBug> beetles = new HashSet<LadyBug>();

        for (Future<Collection<LadyBug>> enlightedTree : enlightenment){
            try {
                beetles.addAll(enlightedTree.get());
            } catch (Exception e) {
                throw new SunShineException("Unable to pick up a beetle :(", e);
            }
        }

        return beetles;
    }

    private void waitForEnlightenment(List<Future<Collection<LadyBug>>> enlighting) {
        beams.shutdown();

        try {
            while (!beams.awaitTermination(10, TimeUnit.SECONDS)) {
                long shiningTime = (System.currentTimeMillis() - shineStartTime) / 1000;

                World.announce("... after " + shiningTime + " secs there are still " + treesToShineOn.get() + " trees in the dark");
            }
        } catch (InterruptedException e) {
            throw new SunShineException("Too impatient to wait for enlightenment :(", e);
        }
    }


    private List<Future<Collection<LadyBug>>> enlighten(List<Tree> trees) {
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