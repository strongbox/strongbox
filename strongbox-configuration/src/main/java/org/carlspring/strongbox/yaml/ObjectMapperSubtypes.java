package org.carlspring.strongbox.yaml;

import org.carlspring.strongbox.util.ServiceLoaderUtils;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.repository.remote.RemoteRepositoryConfigurationDto;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
public class ObjectMapperSubtypes
{

    public static final ObjectMapperSubtypes INSTANCE = new ObjectMapperSubtypes();

    private static final Set<Class<?>> TYPES = ImmutableSet.of(CustomRepositoryConfiguration.class,
                                                               CustomRepositoryConfigurationDto.class,
                                                               RemoteRepositoryConfigurationDto.class);

    private volatile Set<Class<?>> subtypes;

    protected ObjectMapperSubtypes()
    {

    }

    public final synchronized Set<Class<?>> subtypes()
    {
        if (subtypes == null)
        {
            subtypes = ServiceLoaderUtils.load(getTypes().toArray(new Class<?>[0]));
        }
        return subtypes;
    }

    protected Set<Class<?>> getTypes()
    {
        return TYPES;
    }


}
