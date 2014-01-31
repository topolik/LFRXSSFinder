package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.water.Droplet;
import cz.topolik.xssfinder.v2.water.River;

import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public interface RainbowFish {
    public static List<String> UNEATABLE = River.UNEATABLE;
    public static List<String> TASTY = River.TASTY;

    public List<String> swallow(Droplet droplet);
}
