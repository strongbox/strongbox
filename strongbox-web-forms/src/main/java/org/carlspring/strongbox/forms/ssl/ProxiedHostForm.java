package org.carlspring.strongbox.forms.ssl;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.net.Proxy;

/**
 * @author Przemyslaw Fusik
 */
public class ProxiedHostForm
{

    @Nonnull
    @Valid
    private HostForm targetHost;

    @Nonnull
    @Valid
    private HostForm proxyHost;

    @Nonnull
    private Proxy.Type proxyType = Proxy.Type.HTTP;

}
