package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "proxy-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProxyConfiguration
{

    @XmlAttribute
    private String host;

    @XmlAttribute
    private int port;

    @XmlAttribute
    private String username;

    @XmlAttribute
    private String password;

    /**
     * Proxy type (HTTP, SOCKS5, etc)
     */
    @XmlAttribute
    private String type;

    @XmlElement(name = "host")
    @XmlElementWrapper(name = "non-proxy-hosts")
    private List<String> nonProxyHosts = new ArrayList<>();


    public ProxyConfiguration()
    {
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public List<String> getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(List<String> nonProxyHosts)
    {
        this.nonProxyHosts = nonProxyHosts;
    }

    public void addNonProxyHost(String host)
    {
        nonProxyHosts.add(host);
    }

}
