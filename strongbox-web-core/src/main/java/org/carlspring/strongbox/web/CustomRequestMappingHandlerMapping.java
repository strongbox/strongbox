package org.carlspring.strongbox.web;

import org.carlspring.strongbox.configuration.StoragesConfigurationManager;

import javax.inject.Inject;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author sbespalov
 */
public class CustomRequestMappingHandlerMapping
        extends RequestMappingHandlerMapping
{

    @Inject
    private StoragesConfigurationManager configurationManager;

    @Override
    protected RequestCondition<LayoutRequestCondition> getCustomTypeCondition(Class<?> handlerType)
    {
        return Optional.ofNullable(AnnotationUtils.findAnnotation(handlerType, LayoutRequestMapping.class))
                       .map(m -> new LayoutRequestCondition(configurationManager, m.value()))
                       .orElse(null);
    }

}
