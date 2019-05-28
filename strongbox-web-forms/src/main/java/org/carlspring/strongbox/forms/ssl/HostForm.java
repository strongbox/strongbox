package org.carlspring.strongbox.forms.ssl;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.net.InetAddress;

/**
 * @author Przemyslaw Fusik
 */
public class HostForm
{

    @NotNull
    private InetAddress name;

    @Positive
    private Integer port;

    public HostForm()
    {
    }

    public HostForm(@NotEmpty InetAddress name,
                    @Positive Integer port)
    {
        this.name = name;
        this.port = port;
    }

    public InetAddress getName()
    {
        return name;
    }

    public void setName(InetAddress name)
    {
        this.name = name;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public Integer getPortOrDefault(Integer defaultPort)
    {
        return port != null ? port : defaultPort;
    }
}
