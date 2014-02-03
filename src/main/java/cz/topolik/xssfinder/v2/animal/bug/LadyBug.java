package cz.topolik.xssfinder.v2.animal.bug;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Water;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.io.File;

/**
 * @author Tomas Polesovsky
 */
public class LadyBug implements Comparable<LadyBug> {
    public static final LadyBug NO_LADYBUG = new LadyBug(new Droplet("", -1, "", new Tree(new File(""), new File(""))), Water.CLEAN_WATER);
    private Droplet droplet;
    private Water water;

    public LadyBug(Droplet droplet, Water water) {
        this.droplet = droplet;
        this.water = water;
    }

    public String getLineContent() {
        return droplet.getRing();
    }

    public int getLineNum() {
        return droplet.getRingNum();
    }

    public Tree getTree() {
        return droplet.getTree();
    }

    public Droplet getDroplet() {
        return droplet;
    }

    public Water getWater() {
        return water;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LadyBug)) return false;

        LadyBug ladyBug = (LadyBug) o;

        if (droplet != null ? !droplet.equals(ladyBug.droplet) : ladyBug.droplet != null) return false;
        if (water != null ? !water.equals(ladyBug.water) : ladyBug.water != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = droplet != null ? droplet.hashCode() : 0;
        result = 31 * result + (water != null ? water.hashCode() : 0);
        return result;
    }

    public int compareTo(LadyBug o) {
        if (o == null) {
            return 1;
        }
        int result = getTree().getRoot().compareTo(o.getTree().getRoot());
        return result != 0 ? result : (getLineNum() < o.getLineNum() ? -1 : (getLineNum() == o.getLineNum() ? 0 : 1));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String fileName = getTree().getRoot().getAbsolutePath();
        sb.append(fileName);
        sb.append(":");
        sb.append(getLineNum());
        sb.append("\n");
        sb.append(getLineContent());
        sb.append("\n");
        sb.append(water);
        return sb.toString();
    }
}
