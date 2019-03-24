package org.carlspring.strongbox.config;

import org.carlspring.strongbox.yaml.StrongboxYamlMapper;

import javax.annotation.Nonnull;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * @author Przemyslaw Fusik
 */
public class TestingYamlMapper
        extends StrongboxYamlMapper
{

    public TestingYamlMapper(@Nonnull final Set<Class<?>> contextClasses)
    {
        super(contextClasses);
        disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    }
}
