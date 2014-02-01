package cz.topolik.xssfinder.v2.animal.bug;

import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.Collections;
import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public class ChestnutBug implements TreeBug {
    @Override
    public List<LadyBug> meetLadyBugs() {
        return Collections.emptyList();
    }

    @Override
    public void setTree(Tree tree) {

    }
}
