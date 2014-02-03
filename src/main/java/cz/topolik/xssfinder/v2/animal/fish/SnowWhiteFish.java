package cz.topolik.xssfinder.v2.animal.fish;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.Droplet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class SnowWhiteFish implements WhiteFish {
    private static final Pattern VULNERABLE_TAGLIB_LINE_PATTERN = Pattern.compile("^request.getAttribute\\(\"([a-zA-Z-]+:[a-zA-Z-]+:[a-zA-Z-]+)\"\\)$");

    @Override
    public boolean likes(Droplet droplet) {
        Matcher m = VULNERABLE_TAGLIB_LINE_PATTERN.matcher(droplet.getExpression());
        if (m.matches()) {
            World.see().snow().addBlackSpot(m.group(1));
            return true;
        }

        return false;
    }

}
