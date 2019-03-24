package org.carlspring.strongbox.yaml;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

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

        contextClasses.forEach(
                contextClass -> {
                    final String jsonTypeName = getJsonTypeNameValue(contextClass);
                    if (jsonTypeName != null)
                    {
                        registerSubtypes(new NamedType(contextClass, jsonTypeName));
                    }
                });

        registerModules();
    }

    private void registerModules()
    {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addAbstractTypeMapping(Map.class, LinkedHashMap.class);
        simpleModule.addAbstractTypeMapping(Set.class, LinkedHashSet.class);
        this.registerModule(simpleModule);
    }

    private String getJsonTypeNameValue(final Class<?> contextClass)
    {
        final JsonTypeName annotation = contextClass.getAnnotation(JsonTypeName.class);
        if (annotation != null)
        {
            return annotation.value();
        }
        return null;
    }
}
