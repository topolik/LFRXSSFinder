package cz.topolik.xssfinder.v2.water;

/**
 * @author Tomas Polesovsky
 */
public class SnowFlakeChestnutAttribute {
    boolean xssVulnerable = true;
    Boolean escapeXml;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public boolean isXssVulnerable() {
        return xssVulnerable;
    }

    public void setXssVulnerable(boolean xssVulnerable) {
        this.xssVulnerable = xssVulnerable;
    }

    public Boolean getEscapeXml() {
        return escapeXml;
    }

    public void setEscapeXml(Boolean escapeXml) {
        this.escapeXml = escapeXml;
    }
}
