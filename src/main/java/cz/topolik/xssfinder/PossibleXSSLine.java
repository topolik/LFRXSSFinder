package cz.topolik.xssfinder;

import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.Arrays;

/**
 *
 * @author Tomas Polesovsky
 */
public class PossibleXSSLine implements Comparable<PossibleXSSLine>{
    private FileContent sourceFile;
    private long lineNum;
    private String lineContent;
    private String[] stackTrace;

    public PossibleXSSLine(FileContent sourceFile, long lineNum, String lineContent, String[] stackTrace) {
        this.sourceFile = sourceFile;
        this.lineNum = lineNum;
        this.lineContent = lineContent;
        this.stackTrace = stackTrace;
    }

    public PossibleXSSLine(Droplet droplet, String[] stackTrace) {
        this.sourceFile = new FileContent();
        sourceFile.setFile(droplet.getTree().getRoot());
        sourceFile.getContent().addAll(droplet.getTree().getGrowthRings());
        this.lineNum = droplet.getGrowthRingNum();
        this.lineContent = droplet.getGrowthRing();
        this.stackTrace = stackTrace;
    }

    public String getLineContent() {
        return lineContent;
    }

    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    public long getLineNum() {
        return lineNum;
    }

    public void setLineNum(long lineNum) {
        this.lineNum = lineNum;
    }

    public FileContent getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(FileContent sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PossibleXSSLine other = (PossibleXSSLine) obj;
        if (this.sourceFile != other.sourceFile && (this.sourceFile == null || !this.sourceFile.equals(other.sourceFile))) {
            return false;
        }
        if (this.lineNum != other.lineNum) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.sourceFile != null ? this.sourceFile.hashCode() : 0);
        hash = 67 * hash + (int) (this.lineNum ^ (this.lineNum >>> 32));
        return hash;
    }



    public int compareTo(PossibleXSSLine o) {
        if ( o == null) {
            return 1;
        }
        int result = sourceFile.compareTo(o.sourceFile);
        return result != 0 ? result : (lineNum<o.lineNum ? -1 : (lineNum==o.lineNum ? 0 : 1));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String fileName = sourceFile.getFile().getAbsolutePath();
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNum);
        sb.append("\n");
        sb.append(lineContent.trim());
        sb.append("\n");
        sb.append(Arrays.asList(stackTrace));
        return sb.toString();
    }


    
}
