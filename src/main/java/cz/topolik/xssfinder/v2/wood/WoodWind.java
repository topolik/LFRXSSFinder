package cz.topolik.xssfinder.v2.wood;

import cz.topolik.xssfinder.v2.World;

import java.io.File;

/**
 * @author Tomas Polesovsky
 */
public class WoodWind {

    public Forest explore(File... continents) {
        World.announce("Wind explores " + continents.length + " continents ...");

        Forest trees = new Forest();

        for (File continent : continents) {
            breezeThru(continent, trees, continent);
        }

        World.announce(" ... the wind revealed " + trees.size() + " trees");
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

            if (trees.size() % 1000 == 0) {
                World.announce(" ... there are " + trees.size() + " trees plant so far");
            }
        }
    }
}
