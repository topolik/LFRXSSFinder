package cz.topolik.xssfinder.v2.wood;

import cz.topolik.xssfinder.v2.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Tomas Polesovsky
 */
public class Forest extends TreeSet<Tree> {
    boolean antsAreTired = false;

    public void callAntsToExamine() {
        World.announce("Calling ants to examine the forest ... ");

        Iterator<Tree> ant = iterator();
        int pos = 0;
        while (ant.hasNext()) {
            ant.next().examineByAnt();

            pos++;
            if (pos % 100 == 0) {
                World.announce(" ... ants climbed " + pos + " trees so far");
            }
        }

        World.announce(" ... Ants are tired after exploring " + pos + " trees, celebrating :)");
        antsAreTired = true;
    }

    /**
     * Files that are JSPs compiled into Java files using Jasper
     */
    public List<Tree> linden() {
        if (!antsAreTired) {
            throw new AntException("Ants are not tired yet, call ants first!");
        }

        ArrayList<Tree> linden = new ArrayList<Tree>();
        for (Tree tree : this) {
            if (tree.isLinden()) {
                linden.add(tree);
            }
        }

        return linden;
    }

    /**
     * Files that are Java files, no compiled JSP included
     */
    public List<Tree> birches() {
        if (!antsAreTired) {
            throw new AntException("Ants are not tired yet, call ants first!");
        }

        ArrayList<Tree> birches = new ArrayList<Tree>();
        for (Tree tree : this) {
            if (tree.isBirch()) {
                birches.add(tree);
            }
        }

        return birches;
    }

    /**
     * TLD Files
     */
    public List<Tree> chestnuts() {
        if (!antsAreTired) {
            throw new AntException("Ants are not tired yet, call ants first!");
        }

        ArrayList<Tree> chestnuts = new ArrayList<Tree>();
        for (Tree tree : this) {
            if (tree.isChestnut()) {
                chestnuts.add(tree);
            }
        }

        return chestnuts;
    }

}
