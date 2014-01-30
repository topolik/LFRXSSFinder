package cz.topolik.xssfinder.v2.wood;

import java.io.File;

/**
 * @author Tomas Polesovsky
 */
public class Wind {

    public Forest explore(File... continents) {
        Forest trees = new Forest();

        for (File continent : continents) {
            breezeThru(continent, trees, continent);
        }

        return trees;
    }

    protected void breezeThru(File continent, Forest trees, File... forests) {
        if (forests == null || forests.length == 0) {
            return;
        }

        for (File forest : forests) {
            breezeThru(continent, trees, forest.listFiles());
            plant(trees, forest, continent);
        }
    }

    protected void plant(Forest trees, File tree, File continent) {
        if (Tree.isTree(tree)) {
            trees.add(new Tree(tree, continent));
        }
    }
}
