package org.carlspring.strongbox.client;

import java.util.List;

/**
 * @author ankit.tomar
 */
public class ProxyServerConfiguration
{

    private String host;

    private Integer port;

    private String username;

    private String password;

    private String type;

    private List<String> nonProxyHosts;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
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

    @Override
    public String toString()
    {
        return "ProxyServerConfiguration [host=" + host + ", port=" + port + ", username=" + username + ", password="
                + password + ", type=" + type + ", nonProxyHosts=" + nonProxyHosts + "]";
    }

}
