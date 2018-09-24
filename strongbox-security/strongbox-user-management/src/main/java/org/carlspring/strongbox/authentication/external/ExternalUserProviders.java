package org.carlspring.strongbox.authentication.external;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "external-user-providers")
@XmlAccessorType(XmlAccessType.NONE)
public class ExternalUserProviders
{

    @XmlElementRef
    private Set<ExternalUserProvider> providers;

    public Set<ExternalUserProvider> getProviders()
    {
        return providers;
    }

    public void setProviders(final Set<ExternalUserProvider> providers)
    {
        this.providers = providers;
    }

    public void add(final ExternalUserProvider externalUserProvider)
    {
        if (providers == null)
        {
            providers = new LinkedHashSet<>();
        }
        providers.add(externalUserProvider);
    }
}
