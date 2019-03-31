package cz.hudecekpetr.snowride;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlFacade {
    static {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    private static DocumentBuilder dBuilder;

    public static Document loadXmlFromFile(File file) {
        try {
            return dBuilder.parse(file);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
