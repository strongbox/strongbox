package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm.ProxyConfigurationFormChecks;
import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RepositoryFormTestIT
        extends RestAssuredBaseTest
{

    private static final String ID_VALID = "id";
    private static final RepositoryPolicyEnum POLICY_VALID = RepositoryPolicyEnum.RELEASE;
    private static final StorageProviderEnum IMPLEMENTATION_VALID = StorageProviderEnum.FILESYSTEM;
    private static final String LAYOUT_VALID = "Maven 2";
    private static final RepositoryTypeEnum TYPE_VALID = RepositoryTypeEnum.HOSTED;
    private static final RepositoryStatusEnum STATUS_VALID = RepositoryStatusEnum.IN_SERVICE;
    private static final Integer HTTP_CONNECTION_POOL_VALID = 1;
    private static final Integer HTTP_CONNECTION_POOL_INVALID = -1;
    private static final String VALUE_INVALID = "VALUE_INVALID";
    private static ProxyConfigurationForm proxyConfigurationForm;
    private static RemoteRepositoryForm remoteRepositoryForm;
    private static CustomRepositoryConfigurationForm repositoryConfiguration;

    @Inject
    private Validator validator;

    private static Stream<Arguments> policyProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A policy must be specified."),
                Arguments.of(VALUE_INVALID, 1, "A policy value is invalid.")
        );
    }

    private static Stream<Arguments> implementationProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "An implementation must be specified."),
                Arguments.of(VALUE_INVALID, 1, "An implementation value is invalid.")
        );
    }

    private static Stream<Arguments> layoutProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A layout must be specified."),
                Arguments.of(VALUE_INVALID, 1, "A layout value is invalid.")
        );
    }

    private static Stream<Arguments> typeProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A type must be specified."),
                Arguments.of(VALUE_INVALID, 1, "A type value is invalid.")
        );
    }

    private static Stream<Arguments> statusProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A status must be specified."),
                Arguments.of(VALUE_INVALID, 1, "A status value is invalid.")
        );
    }

    private void validateAndAssert(RepositoryForm repositoryForm,
                                   int numErrors,
                                   String errorMessage)
    {
        // when
        Set<ConstraintViolation<RepositoryForm>> violations = validator.validate(repositoryForm,
                                                                                 Default.class,
                                                                                 ProxyConfigurationFormChecks.class);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), numErrors);
        assertThat(violations).extracting("message").containsAnyOf(errorMessage);
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        final String HOST_VALID = "host";
        final int PORT_VALID = 1;
        final String TYPE_VALID = "DIRECT";

        proxyConfigurationForm = new ProxyConfigurationForm();
        proxyConfigurationForm.setHost(HOST_VALID);
        proxyConfigurationForm.setPort(PORT_VALID);
        proxyConfigurationForm.setType(TYPE_VALID);

        final String URL_VALID = "url";
        final Integer CHECK_INTERVAL_SECONDS_VALID = 1;

        remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl(URL_VALID);
        remoteRepositoryForm.setCheckIntervalSeconds(CHECK_INTERVAL_SECONDS_VALID);

        repositoryConfiguration = new MavenRepositoryConfigurationForm();
    }

    @Test
    void testRemoteRepositoryFormValid()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        // when
        Set<ConstraintViolation<RepositoryForm>> violations = validator.validate(repositoryForm,
                                                                                 Default.class,
                                                                                 ProxyConfigurationFormChecks.class);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testRemoteRepositoryFormInvalidEmptyId()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(StringUtils.EMPTY);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 1, "An id must be specified.");
    }

    @ParameterizedTest
    @MethodSource("policyProvider")
    void testRemoteRepositoryFormInvalidPolicy(String policy,
                                               int numErrors,
                                               String errorMessage)
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(policy);
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, numErrors, errorMessage);
    }

    @ParameterizedTest
    @MethodSource("implementationProvider")
    void testRemoteRepositoryFormInvalidImplementation(String implementation,
                                                       int numErrors,
                                                       String errorMessage)
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(implementation);
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, numErrors, errorMessage);
    }

    @ParameterizedTest
    @MethodSource("layoutProvider")
    void testRemoteRepositoryFormInvalidLayout(String layout,
                                               int numErrors,
                                               String errorMessage)
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(layout);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, numErrors, errorMessage);
    }

    @ParameterizedTest
    @MethodSource("typeProvider")
    void testRemoteRepositoryFormInvalidType(String type,
                                             int numErrors,
                                             String errorMessage)
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(type);
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, numErrors, errorMessage);
    }

    @ParameterizedTest
    @MethodSource("statusProvider")
    void testRemoteRepositoryFormInvalidStatus(String status,
                                               int numErrors,
                                               String errorMessage)
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(status);
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, numErrors, errorMessage);
    }

    @Test
    void testRemoteRepositoryFormInvalidProxyConfigurationForm()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());

        proxyConfigurationForm.setHost(StringUtils.EMPTY);
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);

        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 1, "A host must be specified.");
    }

    @Test
    void testRemoteRepositoryFormInvalidRemoteRepositoryConfigurationForm()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);

        remoteRepositoryForm.setUrl(StringUtils.EMPTY);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);

        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 1, "An url must be specified.");
    }

    @Test
    void testRemoteRepositoryFormInvalidNegativeHttpConnectionPool()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setImplementation(IMPLEMENTATION_VALID.describe());
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_INVALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 1, "A httpConnectionPool must be positive or zero.");
    }
}
