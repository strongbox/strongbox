package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Steve Todorov
 */
@XmlRootElement(name = "smtp-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableSmtpConfiguration
        implements Serializable
{

    @XmlAttribute
    private String host;

    @XmlAttribute
    private Integer port;

    @XmlAttribute
    private String connection;

    @XmlAttribute
    private String username;

    @XmlAttribute
    private String password;

    public MutableSmtpConfiguration()
    {
    }

    public MutableSmtpConfiguration(String host,
                                    Integer port,
                                    String connection,
                                    String username,
                                    String password)
    {
        this.host = host;
        this.port = port;
        this.connection = connection;
        this.username = username;
        this.password = password;
    }

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

    public String getConnection()
    {
        return connection;
    }

    public void setConnection(String connection)
    {
        this.connection = connection;
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
}
