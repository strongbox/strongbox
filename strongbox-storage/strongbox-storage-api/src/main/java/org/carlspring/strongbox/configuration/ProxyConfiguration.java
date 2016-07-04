package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyConfiguration that = (ProxyConfiguration) o;
        return port == that.port &&
               Objects.equal(host, that.host) &&
               Objects.equal(username, that.username) &&
               Objects.equal(password, that.password) &&
               Objects.equal(type, that.type) &&
               Objects.equal(nonProxyHosts, that.nonProxyHosts);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(host, port, username, password, type, nonProxyHosts);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                          .add("host", host)
                          .add("port", port)
                          .add("username", username)
                          .add("password", password)
                          .add("type", type)
                          .add("nonProxyHosts", nonProxyHosts)
                          .toString();
    }
}
