package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.RepositoryData;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author carlspring
 */
public interface MavenArtifactCoordinatesValidator extends ArtifactCoordinatesValidator
{


    @Override
    default boolean supports(RepositoryData repository)
    {
        return supports(repository.getLayout());
    }

    @Override
    default boolean supports(String layoutProvider)
    {
        return Maven2LayoutProvider.ALIAS.equals(layoutProvider);
    }

    @Override
    default Set<String> getSupportedLayoutProviders()
    {
        return Sets.newHashSet(Maven2LayoutProvider.ALIAS);
    }

}
