package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.providers.layout.PypiLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author sainalshah
 */
public interface PypiArtifactCoordinatesValidator
        extends ArtifactCoordinatesValidator
{

    @Override
    default boolean supports(Repository repository)
    {
        return supports(repository.getLayout());
    }

    @Override
    default boolean supports(String layoutProvider)
    {
        return PypiLayoutProvider.ALIAS.equals(layoutProvider);
    }

    @Override
    default Set<String> getSupportedLayoutProviders()
    {
        return Sets.newHashSet(PypiLayoutProvider.ALIAS);
    }

}
