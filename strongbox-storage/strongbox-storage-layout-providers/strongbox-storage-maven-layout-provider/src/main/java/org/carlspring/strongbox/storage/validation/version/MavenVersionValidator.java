package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidator;

/**
 * @author Przemyslaw Fusik
 */
interface MavenVersionValidator
        extends VersionValidator
{

    @Override
    default boolean supports(Repository repository)
    {
        return RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout());
    }

    @Override
    default boolean isRelease(String version)
    {
        return version != null && ArtifactUtils.isReleaseVersion(version);
    }

    @Override
    default boolean isSnapshot(String version)
    {
        return version != null && ArtifactUtils.isSnapshot(version);
    }

}
