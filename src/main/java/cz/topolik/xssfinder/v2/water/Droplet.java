package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.wood.Tree;

/**
 * @author Tomas Polesovsky
 */
public class Droplet {
    private String expression;
    private int growthRingNum;
    private String growthRing;
    private Tree tree;

    public Droplet(String expression, int growthRingNum, String growthRing, Tree tree) {
        this.expression = expression;
        this.growthRingNum = growthRingNum;
        this.growthRing = growthRing;
        this.tree = tree;
    }

    public Droplet droppy(String newExpression) {
        return new Droplet(newExpression, growthRingNum, growthRing, tree);
    }

    public Droplet droppy(String newExpression, int newGrowthRingNum) {
        return new Droplet(newExpression, newGrowthRingNum, growthRing, tree);
    }

    public Droplet droppy(String newExpression, int newGrowthRingNum, String newGrowthRing) {
        return new Droplet(newExpression, newGrowthRingNum, newGrowthRing, tree);
    }

    public String getExpression() {
        return expression;
    }

    public int getGrowthRingNum() {
        return growthRingNum;
    }

    public String getGrowthRing() {
        return growthRing;
    }

    public Tree getTree() {
        return tree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Droplet)) return false;

        Droplet droplet = (Droplet) o;

        if (growthRingNum != droplet.growthRingNum) return false;
        if (expression != null ? !expression.equals(droplet.expression) : droplet.expression != null) return false;
        if (growthRing != null ? !growthRing.equals(droplet.growthRing) : droplet.growthRing != null) return false;
        if (tree != null ? !tree.equals(droplet.tree) : droplet.tree != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + growthRingNum;
        result = 31 * result + (growthRing != null ? growthRing.hashCode() : 0);
        result = 31 * result + (tree != null ? tree.hashCode() : 0);
        return result;
    }
}
