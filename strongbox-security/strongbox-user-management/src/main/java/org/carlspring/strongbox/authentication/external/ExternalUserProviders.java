package org.carlspring.strongbox.authentication.external;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "external-user-providers")
@XmlAccessorType(XmlAccessType.NONE)
public class ExternalUserProviders
{

    @XmlElementWrapper(name = "providers")
    private Set<ExternalUserProvider> providers;

    public Set<ExternalUserProvider> getProviders()
    {
        return providers;
    }
}
