package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Przemyslaw Fusik
 */
public class PropertiesAdapter
        extends XmlAdapter<ElementWrapper<Node>, Map<String, String>>
{

    private DocumentBuilder documentBuilder;

    private DocumentBuilder getDocumentBuilder()
            throws Exception
    {
        if (null == documentBuilder)
        {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            documentBuilder = dbf.newDocumentBuilder();
        }
        return documentBuilder;

    }

    @Override
    public ElementWrapper<Node> marshal(final Map<String, String> map)
            throws Exception
    {
        final Document document = getDocumentBuilder().newDocument();
        final List<Node> nodes = new ArrayList<>();
        for (final Map.Entry<String, String> entry : map.entrySet())
        {
            if (entry.getKey() != null && entry.getValue() != null)
            {
                final Element property = document.createElement(entry.getKey());
                property.appendChild(document.createTextNode(entry.getValue()));
                nodes.add(property);
            }
        }
        return new ElementWrapper<>(nodes);
    }

    @Override
    public Map<String, String> unmarshal(final ElementWrapper<Node> properties)
            throws Exception
    {
        final List<Node> props = properties.getElements();
        final Map<String, String> map = new HashMap<>(props.size());
        for (final Node prop : props)
        {
            Node property = prop;
            if (prop instanceof Document)
            {
                property = prop.getFirstChild();
            }
            map.put(property.getNodeName(), ((CharacterData) property.getFirstChild()).getData());
        }
        return map;
    }

}
