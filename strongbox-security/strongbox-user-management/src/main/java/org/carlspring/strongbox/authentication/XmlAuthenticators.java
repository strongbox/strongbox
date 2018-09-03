package org.carlspring.strongbox.authentication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "authenticators")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlAuthenticators
{

    @XmlElement(name = "authenticator")
    private List<XmlAuthenticator> authenticators;

    public List<XmlAuthenticator> getAuthenticators()
    {
        return authenticators;
    }
}
