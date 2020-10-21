package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Set;

/**
 * @author sbespalov
 */
public class MavenFileSystem
        extends LayoutFileSystem
{

    @Inject
    private Maven2LayoutProvider layoutProvider;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator hostedRepositoryIndexCreator;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.PROXY)
    private RepositoryIndexCreator proxyRepositoryIndexCreator;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.GROUP)
    private RepositoryIndexCreator groupRepositoryIndexCreator;

    public MavenFileSystem(PropertiesBooter propertiesBooter,
                           Repository repository,
                           FileSystem storageFileSystem,
                           LayoutFileSystemProvider provider)
    {
        super(propertiesBooter, repository, storageFileSystem, provider);
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return layoutProvider.getDigestAlgorithmSet();
    }

    public RepositoryPath rebuildIndex(Repository repository)
            throws IOException
    {
        if (!mavenRepositoryFeatures.isIndexingEnabled(repository))
        {
            throw new IndexingDisabledException();
        }
        if (repository.isHostedRepository())
        {
            return hostedRepositoryIndexCreator.apply(repository);
        }
        if (repository.isGroupRepository())
        {
            return groupRepositoryIndexCreator.apply(repository);
        }
        if (repository.isProxyRepository())
        {
            return proxyRepositoryIndexCreator.apply(repository);
        }
        throw new IllegalArgumentException("Repository type not recognized. Index cannot be rebuilt.");
    }

}
