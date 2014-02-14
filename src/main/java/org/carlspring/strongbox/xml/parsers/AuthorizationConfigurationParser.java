package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.configuration.AuthorizationConfiguration;
import org.carlspring.strongbox.jaas.Privilege;
import org.carlspring.strongbox.jaas.Role;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * @author mtodorov
 */
public class AuthorizationConfigurationParser
        extends GenericParser<AuthorizationConfiguration>
{

    public XStream getXStreamInstance()
    {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.alias("authorization-configuration", AuthorizationConfiguration.class);
        xstream.alias("role", Role.class);
        xstream.alias("roles", List.class);
        xstream.alias("privilege", Privilege.class);
        xstream.alias("privileges", List.class);

        return xstream;
    }

}
