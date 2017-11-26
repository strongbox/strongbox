package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
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

    @Override
    public ElementWrapper<Node> marshal(final Map<String, String> map)
            throws Exception
    {
        final Document document = new DocumentImpl();
        final List<Node> nodes = new ArrayList<>();
        for (final Map.Entry<String, String> entry : map.entrySet())
        {
            final Element property = document.createElement(entry.getKey());
            property.appendChild(document.createTextNode(entry.getValue()));
            nodes.add(property);
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
