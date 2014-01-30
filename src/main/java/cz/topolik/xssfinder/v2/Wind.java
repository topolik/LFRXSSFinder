package cz.topolik.xssfinder.v2;

import cz.topolik.xssfinder.v2.wood.Forest;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class Wind {
    public static final String DIR_JSPPRECOMPILED = "jsp-precompile";
    public static final String DIR_PORTALIMPL = "portal-impl/src";
    public static final String DIR_PORTALSERVICE = "portal-service/src";
    public static final String DIR_UTILBRIDGES = "util-bridges";
    public static final String DIR_UTILJAVA = "util-java/src";
    public static final String DIR_UTILTAGLIB = "util-taglib/src";

    private static final Pattern JAVA_PATTERN = Pattern.compile("^.*java$");

    public Forest explore(File... continents){
        Forest trees = new Forest();

        for (File continent : continents) {
            breezeThru(continent, trees, continent);
        }

        return trees;
    }

    protected void breezeThru(File continent, Forest trees, File... forests) {
        if(forests == null || forests.length == 0){
            return;
        }

        for (File forest : forests){
            breezeThru(continent, trees, forest.listFiles());
            plant(trees, forest, continent);
        }
    }

    protected void plant(Forest trees, File tree, File continent){
        if (!tree.isDirectory()) {
            trees.add(new Tree(tree, continent));
        }
    }
}
