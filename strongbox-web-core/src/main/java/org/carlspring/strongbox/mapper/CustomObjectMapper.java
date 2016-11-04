package org.carlspring.strongbox.mapper;

import org.carlspring.strongbox.xml.parsers.GenericParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
public class CustomObjectMapper
        extends ObjectMapper
{

    public CustomObjectMapper()
    {
        if (GenericParser.IS_OUTPUT_FORMATTED)
        {
            enable(SerializationFeature.INDENT_OUTPUT);
        }

        disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

}
