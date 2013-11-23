package org.carlspring.strongbox.xml.parsers;

import java.util.Set;

import com.thoughtworks.xstream.XStream;

/**
 * @author mtodorov
 */
public class DummyParser
        extends GenericParser<Dummy>
{

    public XStream getXStreamInstance()
    {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.alias("name", Dummy.class);
        xstream.alias("aliases", Set.class);

        return xstream;
    }

}
