package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.configuration.AuthenticationConfiguration;

import com.thoughtworks.xstream.XStream;

/**
 * @author mtodorov
 */
public class AuthenticationConfigurationParser
        extends GenericParser<AuthenticationConfiguration>
{

    public XStream getXStreamInstance()
    {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.alias("authentication-configuration", AuthenticationConfiguration.class);
        xstream.alias("realm", String.class);

        return xstream;
    }

}
