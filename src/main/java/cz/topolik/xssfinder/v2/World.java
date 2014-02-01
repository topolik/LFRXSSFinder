package cz.topolik.xssfinder.v2;

import cz.topolik.xssfinder.v2.animal.bug.LadyBug;
import cz.topolik.xssfinder.v2.sun.Sun;
import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.Rain;
import cz.topolik.xssfinder.v2.water.River;
import cz.topolik.xssfinder.v2.water.Snow;
import cz.topolik.xssfinder.v2.wood.Forest;
import cz.topolik.xssfinder.v2.wood.WoodWind;

import java.io.*;
import java.util.*;

/**
 * @author Tomas Polesovsky
 */
public class World {
    private static final File DUST = new File(System.getProperty("java.io.tmpdir"), "world.memory.dust.txt");
    private static final World world = new World();

    private Forest forest;
    private Snow snow = new Snow();
    private Rain rain = new Rain();
    private River river = new River();
    private WoodWind wind = new WoodWind();
    private Set<String> memoriesFromDust = new HashSet<String>();
    private List<String> memoriesToDust = new Vector<String>();

    private World() {
        // only God is able to create the World

        fromDust();
    }

    public static World see() {
        return world;
    }

    public static void announce(String prophecy) {
        System.out.println(prophecy);
        System.out.flush();
    }

    public static void memorize(Droplet droplet) {
        world.memoriesToDust.add(droplet.toString());
    }

    public static void announce(String prophecy, Throwable wrath) {
        System.out.println(prophecy);
        wrath.printStackTrace(System.out);
        System.out.flush();
    }

    public void explore(File... continents) {
        forest = wind.explore(continents);

        forest.callAntsToExamine();

        rain.fallDown();
        snow.fly();
        river.flow();
    }

    public Set<LadyBug> rotate(int speed) {
        return new Sun(speed).shine();
    }

    public void jDay() {
        toDust();
    }

    public Forest forest() {
        return forest;
    }

    public Rain rain() {
        return rain;
    }

    public River river() {
        return river;
    }

    public Snow snow() {
        return snow;
    }

    public boolean remembers(Droplet droplet) {
        return memoriesFromDust.contains(droplet.toString());
    }

    protected void fromDust() {
        if (DUST.exists() && DUST.canRead()) {
            try {
                InputStream in = new FileInputStream(DUST);
                try {
                    Scanner s = new Scanner(in);
                    while (s.hasNextLine()) {
                        memoriesFromDust.add(s.nextLine());
                    }
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            } catch (FileNotFoundException e) {
            }
        }

        memoriesToDust.addAll(memoriesFromDust);
    }

    protected void toDust() {
        if (!DUST.exists() || DUST.canWrite()) {
            try {
                BufferedWriter sw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DUST)));
                try {
                    TreeSet<String> sortedHashes = new TreeSet<String>(memoriesToDust);
                    for (String hash : sortedHashes) {
                        sw.write(hash.toString());
                        sw.newLine();
                    }
                } catch (IOException e) {
                } finally {
                    if (sw != null) {
                        try {
                            sw.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            } catch (FileNotFoundException e) {
            }
        }
    }

}