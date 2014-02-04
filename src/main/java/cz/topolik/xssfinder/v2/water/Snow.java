package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.sun.Ray;
import cz.topolik.xssfinder.v2.wood.Tree;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tomas Polesovsky
 */
public class Snow {

    private static final String TLD_DIR_JSP = "/html/taglib/";
    private static final String TLD_DIR_JS_EDITOR = "/html/js/editor/";

    private boolean identifyingBlackSpots = false;

    private Set<String> blackSpots = Collections.synchronizedSet(new HashSet<String>());
    private Map<String, SnowFlakeChestnutStructure> flakesByClassName = new HashMap<String, SnowFlakeChestnutStructure>();
    private Map<String, SnowFlakeChestnutStructure> flakesByAttrNormalizedName = new HashMap<String, SnowFlakeChestnutStructure>();

    public boolean isTagLibJSP(Tree tree) {
        String absPath = tree.getRoot().getAbsolutePath();
        return absPath.contains(TLD_DIR_JSP) || absPath.contains(TLD_DIR_JS_EDITOR);
    }

    public void fly(int velocity) {
        World.announce("Snow is flying around ...");
        World.announce(" ... recognizing black spots using velocity " + velocity + " ... ");
        identifyBlackSpots(velocity);
        World.announce(" ... whirling snow to cover black spots with snow flakes ... ");
        whirlSnow();
        World.announce(" ... snow has settled down, it's clear now :)");
    }

    public void addBlackSpot(String blackSpot) {
        blackSpots.add(blackSpot);
    }

    public void addSnowFlake(SnowFlakeChestnutStructure snowFlake) {
        flakesByClassName.put(snowFlake.getClassName(), snowFlake);
    }

    protected void identifyBlackSpots(int velocity) {
        // discover the vulnerable attrs - black spots via {@see SnowWhiteFish}

        identifyingBlackSpots = true;

        int lastBlackSpots;
        long startTime = System.currentTimeMillis();
        do {
            lastBlackSpots = blackSpots.size();

            ExecutorService executorService = Executors.newFixedThreadPool(velocity);
            for (final Tree linden : World.see().forest().linden()) {
                if (isTagLibJSP(linden)) {
                    executorService.submit(new Ray(linden));
                }
            }

            executorService.shutdown();
            try {
                while(!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    long executionTime = (System.currentTimeMillis() - startTime) / 1000;
                    World.announce(" ... found " + blackSpots.size() + " black spots in " + executionTime + "s ...");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            World.announce(" ... found " + blackSpots.size() + " black spots in the round ...");
        } while (blackSpots.size() > lastBlackSpots);

        identifyingBlackSpots = false;
    }

    protected void whirlSnow() {
        for (SnowFlakeChestnutStructure snowFlake : flakesByClassName.values()) {
            for (SnowFlakeChestnutAttribute attr : snowFlake.getAttributes()) {
                String normalizedName = getFullNormalizedName(snowFlake, attr);
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

                    if (identifyingBlackSpots) {
                        return blackSpots.contains(getFullNormalizedName(snowFlake, attr));
                    }

                    return attr.isXssVulnerable();
                }
            }
        }

        return false;
    }

    protected String getFullNormalizedName(SnowFlakeChestnutStructure snowFlake, SnowFlakeChestnutAttribute attr) {
        StringBuffer sb = new StringBuffer(5);
        sb.append(snowFlake.getShortName());
        sb.append(":");
        sb.append(snowFlake.getName());
        sb.append(":");
        sb.append(attr.getName().toLowerCase());
        return sb.toString();
    }
}