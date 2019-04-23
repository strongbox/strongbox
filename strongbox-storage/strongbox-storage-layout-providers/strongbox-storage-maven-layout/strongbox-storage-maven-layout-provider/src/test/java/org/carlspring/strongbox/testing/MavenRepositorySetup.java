package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.yaml.configuration.repository.MutableMavenRepositoryConfiguration;

public abstract class MavenRepositorySetup implements RepositorySetup
{
    public static class MavenRepositorySetupWithProxyType extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            MutableMavenRepositoryConfiguration repositoryConfiguration = new MutableMavenRepositoryConfiguration();
            repositoryConfiguration.setIndexingEnabled(true);

            repository.setRepositoryConfiguration(repositoryConfiguration);
            repository.setType(RepositoryTypeEnum.PROXY.getType());
        }
    }

    public static class MavenRepositorySetupWithGroupType extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setType(RepositoryTypeEnum.GROUP.getType());
        }
    }

    public static class MavenRepositorySetupWithForbiddenDeleteAndDeloyment extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setAllowsDelete(false);
            repository.setAllowsDeployment(false);
            //repository.setArtsetAsetArtifactCoordinateValidators(
            //new LinkedHashSet<>(mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators()));
        }
    }

    public static class MavenRepositorySetupWithForbiddenDeletes extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setAllowsDelete(false);
            repository.setLayout(Maven2LayoutProvider.ALIAS);
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

    public static class MavenRepositorySetupWithTrashEnabled extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setLayout(Maven2LayoutProvider.ALIAS);
            repository.setTrashEnabled(true);
        }
    }

    public static class MavenHostedRepositorySetup extends MavenRepositorySetup
    {
        @Override
        public void setup(MutableRepository repository)
        {
            repository.setLayout(Maven2LayoutProvider.ALIAS);
            repository.setType(RepositoryTypeEnum.HOSTED.getType());
        }
    }
}
