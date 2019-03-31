package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.XmlFacade;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ExternalLibrary {
    public static ExternalLibrary builtIn = ExternalLibrary.loadFromBuiltInXmlFile("BuiltIn.xml");
    public List<ExternalKeyword> keywords = new ArrayList<>();

    private static ExternalLibrary loadFromBuiltInXmlFile(String filename) {
        try {
            URI uri = ExternalLibrary.class.getResource("/xmls/" + filename).toURI();
            File file = new File(uri);
            return loadFromFile(file);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExternalLibrary loadFromFile(File file) {
        Document document = XmlFacade.loadXmlFromFile(file);
        NodeList keywords = document.getElementsByTagName("kw");
        ExternalLibrary externalLibrary = new ExternalLibrary();
        for (int i = 0; i < keywords.getLength(); i++) {
            Element kw = (Element) keywords.item(i);
            externalLibrary.keywords.add(new ExternalKeyword(kw.getAttribute("name"), externalLibrary));
        }
        return externalLibrary;
    }

    public Image getIcon() {
        return Images.b;
    }


}
