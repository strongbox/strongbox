package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.jaas.Privilege;
import org.carlspring.strongbox.jaas.Role;

import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * @author mtodorov
 */
public class RoleParser
        extends GenericParser<Role>
{

    public XStream getXStreamInstance()
    {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.alias("role", Role.class);
        xstream.alias("roles", List.class);
        xstream.alias("privilege", Privilege.class);
        xstream.alias("privileges", List.class);

        return xstream;
    }

}
