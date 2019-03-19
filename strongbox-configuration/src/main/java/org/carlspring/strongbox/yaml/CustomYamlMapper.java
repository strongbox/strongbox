package org.carlspring.strongbox.yaml;

import javax.annotation.Nonnull;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.stereotype.Component;

/**
 * @author Pablo Tirado
 */
public class CustomYamlMapper
        extends YAMLMapper
{

    public CustomYamlMapper(@Nonnull final Set<Class<?>> contextClasses)
    {
        enable(SerializationFeature.WRAP_ROOT_VALUE);
        enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        setSerializationInclusion(JsonInclude.Include.NON_NULL);

        contextClasses.forEach(contextClass -> registerSubtypes(new NamedType(contextClass)));
    }
}
