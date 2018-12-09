package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class StorageFormTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private PropertiesBooter propertiesBooter;

    private static final String ID_VALID = "new-storage";

    private static final String BASEDIR_VALID = "storages/" + ID_VALID;

    private List<RepositoryForm> repositories;

    @Inject
    private Validator validator;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        ProxyConfigurationForm proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost("host");
        proxyConfigurationForm.setPort(1);
        proxyConfigurationForm.setType("DIRECT");

        RemoteRepositoryForm remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl("url");
        remoteRepositoryForm.setCheckIntervalSeconds(1);

        CustomRepositoryConfigurationForm repositoryConfiguration = new MavenRepositoryConfigurationForm();

        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repositoryForm.setImplementation(StorageProviderEnum.FILESYSTEM.describe());
        repositoryForm.setLayout("Maven 2");
        repositoryForm.setType(RepositoryTypeEnum.HOSTED.getType());
        repositoryForm.setStatus(RepositoryStatusEnum.IN_SERVICE.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(1);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        repositories = Lists.newArrayList(repositoryForm);
    }

    @Test
    void testStorageFormValid()
    {
        // given
        StorageForm storageForm = new StorageForm();
        storageForm.setId(ID_VALID);
        storageForm.setBasedir(propertiesBooter.getVaultDirectory() + "/" + BASEDIR_VALID);
        storageForm.setRepositories(repositories);

        // when
        Set<ConstraintViolation<StorageForm>> violations = validator.validate(storageForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testStorageFormInvalidEmptyId()
    {
        // given
        StorageForm storageForm = new StorageForm();
        storageForm.setId(StringUtils.EMPTY);
        storageForm.setBasedir(propertiesBooter.getVaultDirectory() + "/" + BASEDIR_VALID);
        storageForm.setRepositories(repositories);

        // when
        Set<ConstraintViolation<StorageForm>> violations = validator.validate(storageForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(2);
        assertThat(violations).extracting("message").containsAnyOf("An id must be specified.");
    }

    @Test
    void storageIdShouldDisallowIllegalCharacters()
    {
        // given
        StorageForm storageForm = new StorageForm();
        storageForm.setId("mama*");
        storageForm.setBasedir(propertiesBooter.getVaultDirectory() + "/" + BASEDIR_VALID);
        storageForm.setRepositories(repositories);

        // when
        Set<ConstraintViolation<StorageForm>> violations = validator.validate(storageForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("must match \"[a-zA-Z0-9\\-\\_\\.]+\"");
    }

    @Test
    void testStorageFormInvalidRepositories()
    {
        // given
        StorageForm storageForm = new StorageForm();
        storageForm.setId(ID_VALID);
        storageForm.setBasedir(propertiesBooter.getVaultDirectory() + "/" + BASEDIR_VALID);

        repositories.forEach(r -> r.setHttpConnectionPool(-1));
        storageForm.setRepositories(repositories);

        // when
        Set<ConstraintViolation<StorageForm>> violations = validator.validate(storageForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A httpConnectionPool must be positive or zero.");
    }

}
