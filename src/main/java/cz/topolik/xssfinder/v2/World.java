package cz.topolik.xssfinder.v2;

import cz.topolik.xssfinder.v2.animal.LadyBug;
import cz.topolik.xssfinder.v2.sun.Sun;
import cz.topolik.xssfinder.v2.water.Rain;
import cz.topolik.xssfinder.v2.water.River;
import cz.topolik.xssfinder.v2.water.Snow;
import cz.topolik.xssfinder.v2.wood.Forest;
import cz.topolik.xssfinder.v2.wood.WoodWind;

import java.io.File;
import java.util.Set;

/**
 * @author Tomas Polesovsky
 */
public class World {
    private Forest forest;
    private Snow snow = new Snow();
    private Rain rain = new Rain();
    private River river = new River();
    private WoodWind wind = new WoodWind();
    private static final World world = new World();

    public static World see() {
        return world;
    }

    public static void announce(String prophecy) {
        System.out.println(prophecy);
        System.out.flush();
    }


    public static void announce(String prophecy, Throwable wrath) {
        System.out.println(prophecy);
        wrath.printStackTrace(System.out);
        System.out.flush();
    }

    private World() {
        // only God is able to create the World
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
        river.dryUp();
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
}