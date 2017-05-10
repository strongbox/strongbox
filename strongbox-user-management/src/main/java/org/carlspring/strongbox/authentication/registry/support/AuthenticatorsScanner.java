package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.xml.in.XmlAuthenticator;
import org.carlspring.strongbox.authentication.registry.support.xml.in.XmlAuthenticators;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsScanner
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorsScanner.class);

    private static final GenericParser<XmlAuthenticators> parser = new GenericParser<>(XmlAuthenticators.class);

    private final AuthenticatorsRegistry registry;

    @Inject
    private ApplicationContext applicationContext;

    public AuthenticatorsScanner(AuthenticatorsRegistry registry)
    {
        this.registry = registry;
    }

    public void scanAndReloadRegistry()
    {
        XmlAuthenticators xmlAuthenticators = null;
        try
        {
            xmlAuthenticators = parser.parse(getAuthenticationConfigurationResource().getURL());
        }
        catch (Exception e)
        {
            logger.error("Unable to load authenticators from configuration file.", e);
            throw Throwables.propagate(e);
        }

        final List<Authenticator> authenticators = xmlAuthenticators.getAuthenticators().stream().map(
                toAuthenticator()).collect(Collectors.toList());

        registry.reload(authenticators);
    }

    private Resource getAuthenticationConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("authentication.providers.xml",
                                                                      "etc/conf/strongbox-authentication-providers.xml");
    }

    private Function<XmlAuthenticator, Authenticator> toAuthenticator()
    {
        return authenticator ->
        {
            try
            {
                final Class<?> authenticatorClass = Class.forName(authenticator.getClazz());
                if (!Authenticator.class.isAssignableFrom(authenticatorClass))
                {
                    throw new IllegalAuthenticatorException(authenticatorClass + " is not assignable from " +
                                                            Authenticator.class.getName());
                }
                if (StringUtils.isNotBlank(authenticator.getComponentScanBasePackages()))
                {
                    scanRequiredComponents(
                            StringUtils.stripAll(StringUtils.split(authenticator.getComponentScanBasePackages(), ",")));
                    return (Authenticator) applicationContext.getBean(authenticatorClass);
                }

                return (Authenticator) authenticatorClass.newInstance();
            }
            catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
            {
                throw Throwables.propagate(e);
            }
        };
    }

    private void scanRequiredComponents(String... basePackages)
    {
        if (applicationContext instanceof AnnotationConfigRegistry)
        {
            ((AnnotationConfigRegistry) applicationContext).scan(basePackages);
        }
        else if (applicationContext instanceof BeanDefinitionRegistry)
        {
            new ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) applicationContext).scan(basePackages);
        }
    }


}
