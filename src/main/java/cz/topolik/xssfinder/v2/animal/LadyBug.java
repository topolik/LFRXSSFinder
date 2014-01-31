package cz.topolik.xssfinder.v2.animal;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.Arrays;

/**
 *
 * @author Tomas Polesovsky
 */
public class LadyBug implements Comparable<LadyBug>{
    private Droplet droplet;
    private String[] stackTrace;

    public LadyBug(Droplet droplet, String[] stackTrace) {
        this.droplet = droplet;
        this.stackTrace = stackTrace;
    }

    public String getLineContent() {
        return droplet.getGrowthRing();
    }

    public long getLineNum() {
        return droplet.getGrowthRingNum();
    }

    public Tree getTree() {
        return droplet.getTree();
    }

    public Droplet getDroplet() {
        return droplet;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LadyBug)) return false;

        LadyBug ladyBug = (LadyBug) o;

        if (droplet != null ? !droplet.equals(ladyBug.droplet) : ladyBug.droplet != null) return false;
        if (!Arrays.equals(stackTrace, ladyBug.stackTrace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = droplet != null ? droplet.hashCode() : 0;
        result = 31 * result + (stackTrace != null ? Arrays.hashCode(stackTrace) : 0);
        return result;
    }

    public int compareTo(LadyBug o) {
        if ( o == null) {
            return 1;
        }
        int result = getTree().getRoot().compareTo(o.getTree().getRoot());
        return result != 0 ? result : (getLineNum()<o.getLineNum() ? -1 : (getLineNum()==o.getLineNum()? 0 : 1));
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
        sb.append(Arrays.asList(stackTrace));
        return sb.toString();
    }
}
