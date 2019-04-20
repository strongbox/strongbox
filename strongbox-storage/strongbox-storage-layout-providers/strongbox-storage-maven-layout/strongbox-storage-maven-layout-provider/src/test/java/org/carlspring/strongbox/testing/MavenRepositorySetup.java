package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;

public abstract class MavenRepositorySetup implements RepositorySetup
{

    public static class MavenRepositorySetupWithForbiddenDeleteAndDeloyment extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            //MavenRepositoryFeatures mavenRepositoryFeatures = new MavenRepositoryFeatures();
            repository.setAllowsDelete(false);
            repository.setAllowsDeployment(false);
            //repository.setArtifactCoordinateValidators(new LinkedHashSet<>(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators()));
            //repository.setArtsetAsetArtifactCoordinateValidators(
            //new LinkedHashSet<>(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators()));
        }
    }

    public static class MavenRepositorySetupWithForbiddenDeletes extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            //MavenRepositoryFeatures mavenRepositoryFeatures = new MavenRepositoryFeatures();
            repository.setAllowsDelete(false);
            repository.setLayout(Maven2LayoutProvider.ALIAS);
            //repository.setAllowsDeployment(false);
            //repository.setArtifactCoordinateValidators(new LinkedHashSet<>(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators()));
            //repository.setArtsetAsetArtifactCoordinateValidators(
            //new LinkedHashSet<>(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators()));
        }
    }

    public static class MavenRepositorySetupWithForbiddenRedeloyment extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setAllowsRedeployment(false);
        }
    }

    public static class MavenRepositorySetupWithForbiddenDeleteDeploymentForceDelete extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setType(RepositoryTypeEnum.GROUP.getType());
            repository.setAllowsRedeployment(false);
            repository.setAllowsDelete(false);
            repository.setAllowsForceDeletion(false);
        }
    }
}
