package org.carlspring.strongbox.mapper;

import org.carlspring.strongbox.forms.configuration.CustomRepositoryConfigurationForm;
import org.carlspring.strongbox.util.ServiceLoaderUtils;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public class ObjectMapperSubtypes
{

    private static final Class<?>[] TYPES = new Class<?>[]{ RepositoryConfiguration.class,
                                                            CustomRepositoryConfiguration.class,
                                                            CustomRepositoryConfigurationForm.class };

    private static Set<Class<?>> subtypes;

    public static synchronized Set<Class<?>> subtypes()
    {
        if (subtypes == null)
        {
            subtypes = ServiceLoaderUtils.load(TYPES);
        }
        return subtypes;
    }


}
