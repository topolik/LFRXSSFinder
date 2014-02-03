package cz.topolik.xssfinder.v2.animal.bug;

import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java File
 * @author Tomas Polesovsky
 */
public class BirchBug implements TreeBug {
    protected Tree tree;

    @Override
    public List<LadyBug> meetLadyBugs() {
        List<LadyBug> ladyBugs = new ArrayList<LadyBug>();

        List<String> growthRings = tree.getRings();
        boolean insideComment = false;
        for (int year = 0; year < growthRings.size(); year++) {
            String growthRing = growthRings.get(year);

            // no ladybugs here
            if (growthRing.length() == 0){
                continue;
            }

            // ladybugs are protected in this area - perhaps too young/old to "meet"? :)
            if (growthRing.startsWith("/*")) {
                insideComment = true;
            }
            if (insideComment) {
                if (growthRing.endsWith("*/")) {
                    insideComment = false;
                }
                continue;
            }
            if (growthRing.startsWith("//")) {
                continue;
            }


            ladyBugs.addAll(callLadyBugs(year));
        }

        return ladyBugs;
    }

    @Override
    public void prepare(Tree tree) {
        this.tree = tree;
    }

    protected List<LadyBug> callLadyBugs(int yearOfBirth) {
        // no ladybugs on birches :((
        return Collections.emptyList();
    }
}
