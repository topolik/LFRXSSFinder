package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class Snow {

    private static final String TLD_DIR_JSP = "/html/taglib/";
    private static final String TLD_DIR_JS_EDITOR = "/html/js/editor/";

    private Set<String> blackSpots = Collections.synchronizedSet(new HashSet<String>());
    private Map<String, SnowFlakeChestnutStructure> flakesByClassName = new HashMap<String, SnowFlakeChestnutStructure>();
    private Map<String, SnowFlakeChestnutStructure> flakesByAttrNormalizedName = new HashMap<String, SnowFlakeChestnutStructure>();

    public boolean isTagLibJSP(Tree tree) {
        String absPath = tree.getRoot().getAbsolutePath();
        return absPath.contains(TLD_DIR_JSP) || absPath.contains(TLD_DIR_JS_EDITOR);
    }

    public void fly() {
        World.announce("Snow is flying around ...");
        World.announce(" ... recognizing black spots ... ");
        identifyBlackSpots();
        World.announce(" ... whirling snow to cover black spots with snow flakes ... ");
        whirlSnow();
    }

    public void addBlackSpot(String blackSpot) {
        blackSpots.add(blackSpot);
    }

    public void addSnowFlake(SnowFlakeChestnutStructure snowFlake) {
        flakesByClassName.put(snowFlake.getClassName(), snowFlake);
    }

    protected void identifyBlackSpots() {
        // go one time to know the vulnerable attrs - black spots via {@see SnowWhiteFish}
        for (Tree linden : World.see().forest().linden()) {
            if (isTagLibJSP(linden)) {
                linden.getTreeBug().meetLadyBugs();
            }
        }
    }

    protected void whirlSnow() {
        for (SnowFlakeChestnutStructure snowFlake : flakesByClassName.values()) {
            StringBuffer sb = new StringBuffer(5);
            sb.append(snowFlake.getShortName());
            sb.append(":");
            sb.append(snowFlake.getName());
            sb.append(":");
            String friendlyFlakeName = sb.toString().toLowerCase();
            for (SnowFlakeChestnutAttribute attr : snowFlake.getAttributes()) {
                String normalizedName = friendlyFlakeName + attr.getName().toLowerCase();
                if (attr.isXssVulnerable()) {
                    attr.setXssVulnerable(blackSpots.contains(normalizedName));
                }
            }
        }
    }

    public boolean isBlackSpot(String taglibClassName, String setFieldName) {
        String normalizedFieldName = setFieldName.toLowerCase();

        SnowFlakeChestnutStructure snowFlake = flakesByClassName.get(taglibClassName);
        if (snowFlake != null) {
            for (SnowFlakeChestnutAttribute attr : snowFlake.getAttributes()) {
                if (attr.getName().equals(normalizedFieldName)) {
                    return attr.isXssVulnerable();
                }
            }
        }

        return false;
    }
}