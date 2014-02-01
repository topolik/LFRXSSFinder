package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.animal.bug.LadyBug;
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

    public static Droplet surround(String ladyBug, int yearOfBirth, Tree tree) {
        return new Droplet(ladyBug, yearOfBirth, tree.getRing(yearOfBirth), tree);
    }

    public LadyBug clean() {
        if (World.see().remembers(this)) {
            return LadyBug.NO_LADYBUG;
        }

        Water result = dryUp();

        if (result.equals(Water.CLEAN_WATER)) {
            World.memorize(this);
            return LadyBug.NO_LADYBUG;
        }

        return new LadyBug(this, result);
    }

    public Water dryUp() {
        Water result = new Water();

        if (World.see().rain().shed(this)) {
            return Water.CLEAN_WATER;
        }

        Water riverResult = World.see().river().isEdible(this);
        if (riverResult.equals(Water.CLEAN_WATER)) {
            return Water.CLEAN_WATER;
        }
        result.add(riverResult);

        /*
        TODO: Search container Row Checker (and co.) + ResultRow attrs
         */

        // so we know there is no direct XSS
        // but there can be vulnerable taglib call

        Water snowResult = World.see().snow().melt(this);
        if (snowResult.equals(Water.CLEAN_WATER)) {
            // it's safe
            return Water.CLEAN_WATER;
        }
        result.add(snowResult);

        return result;
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

    @Override
    public String toString() {
        return "Droplet{" +
                "expression='" + expression + '\'' +
                ", ringNum=" + ringNum +
                ", ring='" + ring + '\'' +
                ", tree=" + tree +
                '}';
    }
}
