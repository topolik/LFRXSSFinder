package cz.topolik.xssfinder.v2.butterfly;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.FileLoader;
import cz.topolik.xssfinder.v2.water.Droplet;

/**
 * @author Tomas Polesovsky
 */
public class TreeWhiteButterfly implements WhiteButterfly {
    private String treeName;
    private String content;
    private int growthRingNum = -1;

    public TreeWhiteButterfly(String treeName, String content, int growthRingNum) {
        this.treeName = treeName;
        this.content = content;
        this.growthRingNum = growthRingNum;
    }

    @Override
    public boolean isSafe(Droplet droplet) {
        if((this.growthRingNum == -1 || this.growthRingNum == droplet.getGrowthRingNum()) &&
                droplet.getTree().getRoot().toString().endsWith(treeName) &&
                droplet.getExpression().equalsIgnoreCase(content)){

            return true;
        }

        return false;
    }
}
