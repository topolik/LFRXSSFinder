package cz.topolik.xssfinder.v2;

import cz.topolik.xssfinder.v2.water.Rain;
import cz.topolik.xssfinder.v2.water.River;
import cz.topolik.xssfinder.v2.water.Snow;
import cz.topolik.xssfinder.v2.wood.Forest;
import cz.topolik.xssfinder.v2.wood.Wind;

import java.io.File;

/**
 * @author Tomas Polesovsky
 */
public class World {
    private Forest forest;
    private Snow snow = new Snow();
    private Rain rain = new Rain();
    private River river = new River();
    private Wind wind = new Wind();
    private static final World world = new World();

    public static World see() {
        return world;
    }

    public void rotate(File... continents) {
        forest = wind.explore(continents);

        forest.callAntsToExplore();

        rain.fallDown();
        snow.fly();
        river.flow();
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
}
