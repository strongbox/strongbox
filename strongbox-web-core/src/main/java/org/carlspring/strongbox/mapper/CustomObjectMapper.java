package org.carlspring.strongbox.mapper;

import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
public class CustomObjectMapper
        extends ObjectMapper
{

    @PostConstruct
    public void postConstruct()
    {

        if (GenericParser.IS_OUTPUT_FORMATTED)
        {
            enable(SerializationFeature.INDENT_OUTPUT);
        }
        disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspector = AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
        setAnnotationIntrospector(introspector);
    }


}
