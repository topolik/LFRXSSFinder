package cz.topolik.xssfinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tomas Polesovsky
 */
public class FileContent implements Comparable<FileContent> {

    private File file;
    private List<String> content = new ArrayList<String>();

    public FileContent() {
    }

    public FileContent(File file) {
        this.file = file;
    }

    public String getStringContent() {
        StringBuffer sb = new StringBuffer();
        for (String line : getContent()) {
            sb.append(line);
        }
        return sb.toString();
    }

    public List<String> getContent() {
        return content;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileContent other = (FileContent) obj;
        if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    public int compareTo(FileContent o) {
        return o == null ? -1 : this.file.compareTo(o.file);
    }
}
