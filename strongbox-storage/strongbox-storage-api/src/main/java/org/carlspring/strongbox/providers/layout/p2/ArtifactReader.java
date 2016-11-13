package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class ArtifactReader {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactReader.class);

    public static P2ArtifactCoordinates getArtifact(String repositoryBaseDir, String bundle) {
        try {
            return read(repositoryBaseDir, new ArtifactCollector(bundle));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private static XMLReader createReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        return saxParser.getXMLReader();
    }

    private static <V> V read(String filePath, P2Collector<V> collector) throws ParserConfigurationException, SAXException, IOException {
        XMLReader xmlReader = createReader();
        ArtifactContentHanlder<V> handler = new ArtifactContentHanlder<>(collector);
        xmlReader.setContentHandler(handler);
        xmlReader.parse(filePath);

        return handler.get();
    }
}
