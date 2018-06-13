package org.carlspring.strongbox.configuration;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 * @see MutableProxyConfiguration
 */
@Immutable
public class ProxyConfiguration
{

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    private final String type;

    private final List<String> nonProxyHosts;

    public ProxyConfiguration(final MutableProxyConfiguration delegate)
    {
        this.host = delegate.getHost();
        this.port = delegate.getPort();
        this.username = delegate.getUsername();
        this.password = delegate.getPassword();
        this.type = delegate.getType();
        this.nonProxyHosts = immuteNonProxyHosts(delegate.getNonProxyHosts());
    }

    private List<String> immuteNonProxyHosts(final List<String> source)
    {
        return source != null ? ImmutableList.copyOf(source) : Collections.emptyList();
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getType()
    {
        return type;
    }

    public List<String> getNonProxyHosts()
    {
        return nonProxyHosts;
    }
}
