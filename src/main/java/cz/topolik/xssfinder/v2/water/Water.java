package cz.topolik.xssfinder.v2.water;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tomas Polesovsky
 */
public class Water {
    public static final Water CLEAN_WATER = new Water();
    public static final Water UNKNOWN_WATER = new Water();

    private List<String> waves = new ArrayList<String>();

    public void add(String wave) {
        waves.add(wave);
    }

    public void add(Water water) {
        waves.addAll(water.waves);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o == this;
    }

    public int size() {
        return waves.size();
    }

    public List<String> getWaves() {
        return Collections.unmodifiableList(waves);
    }

    public String get(int waveNum) {
        return waves.get(waveNum);
    }
}
