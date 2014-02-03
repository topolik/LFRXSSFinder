package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tomas Polesovsky
 */
public class SnowFlakeChestnutStructure {
    private String name;
    private String className;
    private List<SnowFlakeChestnutAttribute> attributes = new ArrayList<SnowFlakeChestnutAttribute>();
    private Boolean escapeXML;
    private Tree classTree;
    private String shortName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<SnowFlakeChestnutAttribute> getAttributes() {
        return attributes;
    }

    public Boolean getEscapeXML() {
        return escapeXML;
    }

    public void setEscapeXML(Boolean escapeXML) {
        this.escapeXML = escapeXML;
    }

    public Tree getClassTree() {
        return classTree;
    }

    public void setClassTree(Tree classTree) {
        this.classTree = classTree;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName.toLowerCase();
    }

    public String getShortName() {
        return shortName;
    }
}
