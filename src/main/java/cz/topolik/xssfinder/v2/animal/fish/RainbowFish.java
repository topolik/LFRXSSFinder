package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.River;
import cz.topolik.xssfinder.v2.water.Water;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static cz.topolik.xssfinder.v2.water.Water.UNKNOWN_WATER;
import static cz.topolik.xssfinder.v2.water.Water.CLEAN_WATER;

/**
 * @author Tomas Polesovsky
 */
public interface RainbowFish {

    public Water swallow(Droplet droplet);
}
