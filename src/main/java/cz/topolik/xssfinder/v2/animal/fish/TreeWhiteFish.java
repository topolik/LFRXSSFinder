package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;

/**
 * @author Tomas Polesovsky
 */
public class TreeWhiteFish implements WhiteFish {
    private String treeName;
    private String content;
    private int growthRingNum = -1;

    public TreeWhiteFish(String treeName, String content, int growthRingNum) {
        this.treeName = treeName;
        this.content = content;
        this.growthRingNum = growthRingNum;
    }

    @Override
    public boolean likes(Droplet droplet) {
        if ((this.growthRingNum == -1 || this.growthRingNum == droplet.getRingNum()) &&
                droplet.getTreeRoot().toString().endsWith(treeName) &&
                droplet.getExpression().equalsIgnoreCase(content)) {

            return true;
        }

        return false;
    }
}
