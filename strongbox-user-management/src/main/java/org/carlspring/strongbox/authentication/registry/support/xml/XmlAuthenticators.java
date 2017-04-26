package org.carlspring.strongbox.authentication.registry.support.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "authenticators")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAuthenticators
{

    @XmlElement(name = "authenticator")
    private Set<XmlAuthenticator> authenticators = new LinkedHashSet<>();

    public Set<XmlAuthenticator> getAuthenticators()
    {
        return authenticators;
    }
}
