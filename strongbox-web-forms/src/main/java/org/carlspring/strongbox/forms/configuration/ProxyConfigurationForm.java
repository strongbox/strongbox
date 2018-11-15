package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

/**
 * @author Pablo Tirado
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProxyConfigurationForm
{

    @NotBlank(message = "A host must be specified.")
    private String host;

    @Min(value = 1, message = "Port number must be an integer between 1 and 65535.")
    @Max(value = 65535, message = "Port number must be an integer between 1 and 65535.")
    private int port;

    @NotBlank(message = "A proxy type must be specified.")
    @Pattern(regexp = "DIRECT|HTTP|SOCKS4|SOCKS5",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Proxy type must contain one the following strings as value: DIRECT, HTTP, SOCKS4, SOCKS5")
    private String type;

    private String username;

    private String password;

    private List<String> nonProxyHosts = Lists.newArrayList();

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

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
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

    public List<String> getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(List<String> nonProxyHosts)
    {
        this.nonProxyHosts = nonProxyHosts;
    }

    public MutableProxyConfiguration getMutableProxyConfiguration()
    {
        return new MutableProxyConfiguration(this.host, this.port, this.username, this.password, this.type, this.nonProxyHosts);
    }
}
