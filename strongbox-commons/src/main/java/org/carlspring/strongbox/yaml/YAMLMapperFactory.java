package org.carlspring.strongbox.yaml;

import javax.annotation.Nonnull;
import java.util.Set;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author Przemyslaw Fusik
 */
public interface YAMLMapperFactory
{
    YAMLMapper create(@Nonnull final Set<Class<?>> contextClasses);
}
