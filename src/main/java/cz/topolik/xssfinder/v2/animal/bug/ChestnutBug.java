package cz.topolik.xssfinder.v2.animal.bug;

import cz.topolik.xssfinder.v2.World;
import cz.topolik.xssfinder.v2.water.SnowFlakeChestnutAttribute;
import cz.topolik.xssfinder.v2.water.SnowFlakeChestnutStructure;
import cz.topolik.xssfinder.v2.wood.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tomas Polesovsky
 */
public class ChestnutBug implements TreeBug {
    private static final Pattern SAFE_TYPES = Pattern.compile("(byte|short|int|long|float|double|boolean|(java.lang.)?(Byte|Short|Integer|Long|Float|Double|Boolean|Class)|java.util.Locale)(\\[\\])?");

    @Override
    public List<LadyBug> meetLadyBugs() {
        return Collections.emptyList();
    }

    @Override
    public void prepare(Tree tree) {

        //<taglib>
        //  <short-name>liferay-ui</short-name>
        //  <tag>
        //    <name>asset-categories-navigation</name>
        //    <tag-class>com.liferay.taglib.ui.AssetCategoriesNavigationTag</tag-class>


        Document doc = getDoc(tree);
        String shortName = doc.getElementsByTagName("short-name").item(0).getTextContent();
        NodeList tags = doc.getElementsByTagName("tag");
        for (int i = 0; i < tags.getLength(); i++) {
            Node tag = tags.item(i);
            SnowFlakeChestnutStructure snowFlakeChestnutStructure = new SnowFlakeChestnutStructure();
            snowFlakeChestnutStructure.setShortName(shortName);

            for (int j = 0; j < tag.getChildNodes().getLength(); j++) {
                Node child = tag.getChildNodes().item(j);
                if ("name".equals(child.getNodeName())) {
                    snowFlakeChestnutStructure.setName(child.getTextContent());

                } else if ("tag-class".equals(child.getNodeName())) {
                    snowFlakeChestnutStructure.setClassName(child.getTextContent());

                } else if ("attribute".equals(child.getNodeName())) {
                    SnowFlakeChestnutAttribute snowFlakeChestnutAttribute = new SnowFlakeChestnutAttribute();
                    snowFlakeChestnutStructure.getAttributes().add(snowFlakeChestnutAttribute);

                    for (int k = 0; k < child.getChildNodes().getLength(); k++) {
                        Node attributeDef = child.getChildNodes().item(k);
                        if ("name".equals(attributeDef.getNodeName())) {
                            snowFlakeChestnutAttribute.setName(attributeDef.getTextContent());
                        } else if ("type".equals(attributeDef.getNodeName())) {
                            String type = attributeDef.getTextContent();
                            if (SAFE_TYPES.matcher(type).matches()) {
                                snowFlakeChestnutAttribute.setXssVulnerable(false);
                                break;
                            }
                        }
                    }

                    if (snowFlakeChestnutAttribute.getName().equals("escapeXml")) {
                        snowFlakeChestnutStructure.setEscapeXML(Boolean.FALSE);
                    }
                }
            }

            String tagJavaFileName = snowFlakeChestnutStructure.getClassName().replace('.', '/') + ".java";
            for (Tree birch : World.see().forest().birches()) {
                if (birch.getName().endsWith(tagJavaFileName)) {
                    snowFlakeChestnutStructure.setClassTree(birch);

                    if (snowFlakeChestnutStructure.getEscapeXML() != null) {
                        for (String line : birch.getRings()) {
                            if (line.contains("oolean _escapeXml") && line.endsWith("true;")) {
                                snowFlakeChestnutStructure.setEscapeXML(Boolean.TRUE);
                            }
                        }
                    }
                }
            }

            World.see().snow().addSnowFlake(snowFlakeChestnutStructure);
        }
    }

    private Document getDoc(Tree tree) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            domFactory.setValidating(false);
            domFactory.setExpandEntityReferences(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();

            return builder.parse(new ByteArrayInputStream(tree.melt().getBytes()));
        } catch (Exception e) {
            throw new TreeBugException("Unable to create doc for " + tree, e);
        }
    }
}


