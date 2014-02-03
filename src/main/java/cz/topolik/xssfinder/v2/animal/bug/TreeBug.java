package cz.topolik.xssfinder.v2.animal.bug;

import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public interface TreeBug {
    public List<LadyBug> meetLadyBugs();

    public void prepare(Tree tree);
}
