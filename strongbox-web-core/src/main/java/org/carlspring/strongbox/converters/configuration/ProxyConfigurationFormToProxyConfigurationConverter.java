package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class ProxyConfigurationFormToProxyConfigurationConverter
        implements Converter<ProxyConfigurationForm, ProxyConfiguration>
{

    @Override
    public ProxyConfiguration convert(ProxyConfigurationForm proxyConfigurationForm)
    {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setHost(proxyConfigurationForm.getHost());
        proxyConfiguration.setPort(proxyConfigurationForm.getPort());
        proxyConfiguration.setType(proxyConfigurationForm.getType());
        proxyConfiguration.setUsername(proxyConfigurationForm.getUsername());
        proxyConfiguration.setPassword(proxyConfigurationForm.getPassword());
        proxyConfiguration.setNonProxyHosts(proxyConfigurationForm.getNonProxyHosts());

        return proxyConfiguration;
    }
}
