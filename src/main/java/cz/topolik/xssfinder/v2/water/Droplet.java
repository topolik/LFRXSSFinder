package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.FileContent;
import cz.topolik.xssfinder.v2.wood.Tree;

/**
 * @author Tomas Polesovsky
 */
public class Droplet {
    private String expression;
    private int growthRingNum;
    private String growthRing;
    private Tree tree;

    /* TODO: DEPRECATE */
    private FileContent fileContent;

    public Droplet(String expression, int growthRingNum, String growthRing, Tree tree) {
        this.expression = expression;
        this.growthRingNum = growthRingNum;
        this.growthRing = growthRing;
        this.tree = tree;
    }

    public Droplet(String expression, int growthRingNum, String growthRing, FileContent fileContent) {
        this.expression = expression;
        this.growthRingNum = growthRingNum;
        this.growthRing = growthRing;
        this.fileContent = fileContent;
    }

    public Droplet droppy(String newExpression){
        return new Droplet(newExpression, growthRingNum, growthRing, fileContent);
    }

    public Droplet droppy(String newExpression, int newGrowthRingNum){
        return new Droplet(newExpression, newGrowthRingNum, growthRing, fileContent);
    }

    public Droplet droppy(String newExpression, int newGrowthRingNum, String newGrowthRing){
        return new Droplet(newExpression, newGrowthRingNum, newGrowthRing, fileContent);
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

    public FileContent getFileContent() {
        return fileContent;
    }
}
