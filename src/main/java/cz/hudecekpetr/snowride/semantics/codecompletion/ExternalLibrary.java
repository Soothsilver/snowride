package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.XmlFacade;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalLibrary {
    public static ExternalLibrary builtIn = ExternalLibrary.loadFromBuiltInXmlFile("BuiltIn.xml");
    public static Map<String, ExternalLibrary> otherPackedInLibraries = new HashMap<>();
    public List<ExternalKeyword> keywords = new ArrayList<>();

    static {
        declarePackedLibrary("BuiltIn");
        declarePackedLibrary("Collections");
        declarePackedLibrary("DateTime");
        declarePackedLibrary("Dialogs");
        declarePackedLibrary("OperatingSystem");
        declarePackedLibrary("Process");
        declarePackedLibrary("Screenshot");
        declarePackedLibrary("String");
        declarePackedLibrary("Telnet");
        declarePackedLibrary("XML");
    }

    private String name;

    private static void declarePackedLibrary(String libraryName) {
        otherPackedInLibraries.put(libraryName, ExternalLibrary.loadFromBuiltInXmlFile(libraryName + ".xml"));
    }

    private static ExternalLibrary loadFromBuiltInXmlFile(String filename) {
        InputStream inputStream = ExternalLibrary.class.getResourceAsStream("/xmls/" + filename);
        return loadFromInputStream(inputStream);
    }

    private static ExternalLibrary loadFromInputStream(InputStream inputStream) {
        Document document = XmlFacade.loadXmlFromInputStream(inputStream);
        NodeList keywords = document.getElementsByTagName("kw");
        ExternalLibrary externalLibrary = new ExternalLibrary();
        externalLibrary.name = document.getDocumentElement().getAttribute("name");
        for (int i = 0; i < keywords.getLength(); i++) {
            Element kw = (Element) keywords.item(i);
            NodeList docs = kw.getElementsByTagName("doc");
            String doc = "(documentation not provided)";
            if (docs.getLength() == 1) {
                Element doce = (Element) docs.item(0);
                doc = doce.getTextContent();
            }
            doc = doc.replace(" +", " ").replace("\n \n", "\n\n").replace("\n\n","[[DOUBLENEWLINE]]")
                    .replace("\n", " ").replace(" +", " ").replace("[[DOUBLENEWLINE]]", "\n\n");
            externalLibrary.keywords.add(new ExternalKeyword(kw.getAttribute("name"), doc, externalLibrary));
        }
        return externalLibrary;
    }

    public Image getIcon() {
        return Images.b;
    }

    @Override
    public String toString() {
        return name;
    }
}
