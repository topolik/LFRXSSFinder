package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.wood.Tree;

import java.io.File;

/**
 * @author Tomas Polesovsky
 */
public class Droplet {
    private String expression;
    private int ringNum;
    private String ring;
    private Tree tree;

    public Droplet(String expression, int ringNum, String ring, Tree tree) {
        this.expression = expression;
        this.ringNum = ringNum;
        this.ring = ring;
        this.tree = tree;
    }

    public Droplet droppy(String newExpression) {
        return new Droplet(newExpression, ringNum, ring, tree);
    }

    public Droplet droppy(String newExpression, int newGrowthRingNum) {
        return new Droplet(newExpression, newGrowthRingNum, ring, tree);
    }

    public Droplet droppy(String newExpression, int newGrowthRingNum, String newGrowthRing) {
        return new Droplet(newExpression, newGrowthRingNum, newGrowthRing, tree);
    }

    public String getExpression() {
        return expression;
    }

    public int getRingNum() {
        return ringNum;
    }

    public String getRing() {
        return ring;
    }

    public String getRing(int ringNum) {
        return tree.getRing(ringNum);
    }

    public Tree getTree() {
        return tree;
    }

    public File getTreeRoot() {
        return tree.getRoot();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Droplet)) return false;

        Droplet droplet = (Droplet) o;

        if (ringNum != droplet.ringNum) return false;
        if (expression != null ? !expression.equals(droplet.expression) : droplet.expression != null) return false;
        if (ring != null ? !ring.equals(droplet.ring) : droplet.ring != null) return false;
        if (tree != null ? !tree.equals(droplet.tree) : droplet.tree != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + ringNum;
        result = 31 * result + (ring != null ? ring.hashCode() : 0);
        result = 31 * result + (tree != null ? tree.hashCode() : 0);
        return result;
    }
}
