package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "authentication-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthenticationConfiguration
{

    @XmlElement(name = "realm")
    @XmlElementWrapper(name = "realms")
    private List<String> realms = new ArrayList<>();

    @XmlElement(name = "anonymous-access")
    private AnonymousAccessConfiguration anonymousAccessConfiguration;


    public AuthenticationConfiguration()
    {
    }

    public List<String> getRealms()
    {
        return realms;
    }

    public void setRealms(List<String> realms)
    {
        this.realms = realms;
    }

    public boolean addRealm(String realm)
    {
        return realms.add(realm);
    }

    public boolean removeRealm(String realm)
    {
        return realms.remove(realm);
    }

    public boolean containsRealm(String realm)
    {
        return realms.contains(realm);
    }

    public AnonymousAccessConfiguration getAnonymousAccessConfiguration()
    {
        return anonymousAccessConfiguration;
    }

    public void setAnonymousAccessConfiguration(AnonymousAccessConfiguration anonymousAccessConfiguration)
    {
        this.anonymousAccessConfiguration = anonymousAccessConfiguration;
    }

}
