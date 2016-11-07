package org.carlspring.strongbox.mapper;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.xml.parsers.GenericParser.IS_OUTPUT_FORMATTED;

/**
 * @author Alex Oreshkevich
 */
@Component
public class CustomJaxb2RootElementHttpMessageConverter
        extends Jaxb2RootElementHttpMessageConverter
{

    @Override
    protected void customizeMarshaller(Marshaller marshaller)
    {
        super.customizeMarshaller(marshaller);
        try
        {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, IS_OUTPUT_FORMATTED);
        }
        catch (PropertyException e)
        {
            throw new RuntimeException(e);
        }
    }
}
