package cz.topolik.xssfinder.v2.sun;

import cz.topolik.xssfinder.v2.animal.bug.LadyBug;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tomas Polesovsky
 */
public class Ray implements Callable<Collection<LadyBug>> {
    private Tree tree;
    private AtomicInteger treesTotal;

    public Ray(Tree tree) {
        this.tree = tree;
    }

    public Ray(Tree tree, AtomicInteger treesTotal) {
        this.tree = tree;
        this.treesTotal = treesTotal;
    }

    @Override
    public Collection<LadyBug> call() {
        try {
            return tree.getTreeBug().meetLadyBugs();
        } finally {
            if(treesTotal != null) {
                treesTotal.decrementAndGet();
            }
        }
    }


}
