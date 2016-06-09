package org.carlspring.strongbox.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
public class CustomObjectMapper extends ObjectMapper
{

    public CustomObjectMapper(){
        disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}
