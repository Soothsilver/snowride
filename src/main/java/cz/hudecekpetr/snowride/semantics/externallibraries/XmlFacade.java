package cz.hudecekpetr.snowride.semantics.externallibraries;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public static Document loadXmlFromInputStream(InputStream inputStream) {
        try {
            return dBuilder.parse(inputStream);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
