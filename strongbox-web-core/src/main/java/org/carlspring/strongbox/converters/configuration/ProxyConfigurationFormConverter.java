package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public enum ProxyConfigurationFormConverter
        implements Converter<ProxyConfigurationForm, MutableProxyConfiguration>
{

    INSTANCE;

    @Override
    public MutableProxyConfiguration convert(ProxyConfigurationForm proxyConfigurationForm)
    {
        MutableProxyConfiguration proxyConfiguration = new MutableProxyConfiguration();
        proxyConfiguration.setHost(proxyConfigurationForm.getHost());
        proxyConfiguration.setPort(proxyConfigurationForm.getPort());
        proxyConfiguration.setType(proxyConfigurationForm.getType());
        proxyConfiguration.setUsername(proxyConfigurationForm.getUsername());
        proxyConfiguration.setPassword(proxyConfigurationForm.getPassword());
        proxyConfiguration.setNonProxyHosts(proxyConfigurationForm.getNonProxyHosts());

        return proxyConfiguration;
    }
}
