package cz.topolik.xssfinder.v2.water;

import cz.topolik.xssfinder.scan.Logger;
import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.wood.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.topolik.xssfinder.scan.advanced.Constants.OUT_PRINT;

/**
 * @author Tomas Polesovsky
 */
public class Snow {

    private static final Pattern TAGLIB_CALL_PATTERN = Pattern.compile("^(_jspx_th_[\\w]+)\\.set([\\w]+)\\((.*)\\);$");
    //[String id = (String)request.getAttribute("liferay-ui:upload-progress:id");]
    private static final Pattern VULNERABLE_TAGLIB_LINE_PATTERN = Pattern.compile("^.*request.getAttribute\\(\"([a-zA-Z-]+):([a-zA-Z-]+):([a-zA-Z-]+)\"\\).*$");
    private static final String TLD_DIR_JSP = "/html/taglib/";
    private static final String TLD_DIR_JS_EDITOR = "/html/js/editor/";

    private Map<String, Set<String>> vulnerableTaglibs = new HashMap<String, Set<String>>();

    public boolean isTagLibJSP(Tree tree) {
        String absPath = tree.getRoot().getAbsolutePath();
        return absPath.contains(TLD_DIR_JSP) || absPath.contains(TLD_DIR_JS_EDITOR);
    }

    public String[] isLineVulnerableTaglib(Droplet droplet) {
        if (isTagLibJSP(droplet.getTree())) {
            return null;
        }

        Matcher m = TAGLIB_CALL_PATTERN.matcher(droplet.getGrowthRing());
        if (!m.matches()) {
            return null;
        }
        String taglibVariableName = m.group(1);
        String setFieldName = m.group(2);
        String normalizedFieldName = setFieldName.toLowerCase();
        String argument = m.group(3);

        String taglibClassName = null;
        // find declaration of the used taglib
        String declarationLine = null;
        Pattern taglibDeclaration = Pattern.compile("^.*(com.liferay.taglib.[^ ]+) " + taglibVariableName + " (=|:).*$");
        for (int i = droplet.getGrowthRingNum(); i >= 0 && taglibClassName == null; i--) {
            String fileLine = droplet.getTree().getGrowthRings().get(i).trim();
            Matcher declM = taglibDeclaration.matcher(fileLine);
            if (!declM.matches()) {
                continue;
            }
            declarationLine = fileLine;
            taglibClassName = declM.group(1);
        }
        if (taglibClassName != null) {
            if (vulnerableTaglibs.containsKey(taglibClassName)) {
                for (String vulnerableProperty : vulnerableTaglibs.get(taglibClassName)) {
                    if (vulnerableProperty.equals(normalizedFieldName)) {
                        return new String[]{argument, droplet.getGrowthRing(), declarationLine};
                    }
                }
            }
        }
        // line is taglib-safe
        return null;
    }

    public void fly() {
        /*  TODO: taglibs are temporarily disabled */
        if (true) return;


        /** Taglib: shortName -> (tagName, tagClass)<br /> e.g. <code>liferay-ui -> asset-categories-error -> com.liferay.taglib.ui.AssetCategoriesErrorTag</code> */
        Map<String, Map<String, String>> taglibStructure = null;

        try {
            taglibStructure = loadTaglibTLDs();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot parse TLDs: " + ex.getMessage(), ex);
        }


        Logger.log(" ... recognizing vulnerable taglibs ... ");
        getVulnerableTaglibs(taglibStructure);
        Logger.log(" ... recognizing vulnerable taglibs ... finished with " + vulnerableTaglibs.size() + " entries");
    }

    protected Map<String, Map<String, String>> loadTaglibTLDs() throws ParserConfigurationException, SAXException, IOException {
        Map<String, Map<String, String>> taglibStructure = new HashMap<String, Map<String, String>>();

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(false);
        domFactory.setExpandEntityReferences(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();


        //<taglib>
        //  <short-name>liferay-ui</short-name>
        //  <tag>
        //    <name>asset-categories-navigation</name>
        //    <tag-class>com.liferay.taglib.ui.AssetCategoriesNavigationTag</tag-class>

        for (Tree chestnut : World.see().forest().chestnuts()) {
            Document doc = builder.parse(new ByteArrayInputStream(chestnut.melt().getBytes()));
            String shortName = doc.getElementsByTagName("short-name").item(0).getTextContent();
            if (!taglibStructure.containsKey(shortName)) {
                taglibStructure.put(shortName, new HashMap<String, String>());
            }

            NodeList tags = doc.getElementsByTagName("tag");
            for (int i = 0; i < tags.getLength(); i++) {
                Node tag = tags.item(i);
                String tagName = null;
                String tagClass = null;
                for (int j = 0; j < tag.getChildNodes().getLength(); j++) {
                    Node child = tag.getChildNodes().item(j);
                    if ("name".equals(child.getNodeName())) {
                        tagName = child.getTextContent();
                    }
                    if ("tag-class".equals(child.getNodeName())) {
                        tagClass = child.getTextContent();
                    }
                }
                taglibStructure.get(shortName).put(tagName, tagClass);
            }
        }
        return taglibStructure;
    }

    protected void getVulnerableTaglibs(Map<String, Map<String, String>> taglibStructure) {
        for (Tree linden : World.see().forest().linden()) {
            if (isTagLibJSP(linden)) {
                for (int lineNum = 0; lineNum < linden.getGrowthRings().size(); lineNum++) {
                    String line = linden.getGrowthRings().get(lineNum);

                    String trimmed = line.trim();
                    if (!trimmed.startsWith(OUT_PRINT)) {
                        continue;
                    }
                    String functionArgument = trimmed.substring(OUT_PRINT.length(), trimmed.length() - 2).trim();

                    List<String> declaration = World.see().river().isCallArgumentSuspected(new Droplet(functionArgument, lineNum, functionArgument, linden));

                    if (declaration != River.RESULT_SAFE && declaration.size() > 0) {
                        // the last line in the stack should be variable declaration
                        String declarationLine = declaration.get(declaration.size() - 1);

                        // TODO: add also variables which are initialized in pageContext / request attributes

                        Matcher m = VULNERABLE_TAGLIB_LINE_PATTERN.matcher(declarationLine);
                        if (m.matches()) {
                            String taglibShortName = m.group(1);
                            String tagName = m.group(2);
                            String attrName = m.group(3);
                            String normalizedAttrName = attrName.toLowerCase();

                            try {
                                String taglibClassName = taglibStructure.get(taglibShortName).get(tagName);
                                if (!vulnerableTaglibs.containsKey(taglibClassName)) {
                                    vulnerableTaglibs.put(taglibClassName, new HashSet<String>());
                                }
                                vulnerableTaglibs.get(taglibClassName).add(normalizedAttrName);
                            } catch (NullPointerException e) {
                                throw new RuntimeException("Cannot find " + taglibShortName, e);
                            }
                        }
                    }

                }
            }
        }
    }
}
