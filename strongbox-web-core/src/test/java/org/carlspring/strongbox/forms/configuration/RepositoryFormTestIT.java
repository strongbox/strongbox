package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm.ProxyConfigurationFormChecks;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.storage.FileSystemStorageProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RepositoryFormTestIT
        extends RestAssuredBaseTest
{

    private static final String ID_VALID = "id";

    private static final RepositoryPolicyEnum POLICY_VALID = RepositoryPolicyEnum.RELEASE;

    private static final String LAYOUT_VALID = Maven2LayoutProvider.ALIAS;

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
                Arguments.of(VALUE_INVALID, 1, "The policy value is invalid.")
        );
    }

    private static Stream<Arguments> storageProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A storage provider must be specified."),
                Arguments.of(VALUE_INVALID, 1, "The storage provider value is invalid.")
        );
    }

    private static Stream<Arguments> layoutProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A layout must be specified."),
                Arguments.of(VALUE_INVALID, 1, "The layout value is invalid.")
        );
    }

    private static Stream<Arguments> typeProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A type must be specified."),
                Arguments.of(VALUE_INVALID, 1, "The type value is invalid.")
        );
    }

    private static Stream<Arguments> statusProvider()
    {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, 2, "A status must be specified."),
                Arguments.of(VALUE_INVALID, 1, "The status value is invalid.")
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
        assertThat(violations).as("Violations are empty!").hasSize(numErrors);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRemoteRepositoryFormInvalidEmptyId()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(StringUtils.EMPTY);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 2, "An id must be specified.");
    }

    @Test
    void repositoryIdShouldDisallowIllegalCharacters()
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId("mama*");
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_VALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 1, getPatternLocalisedMessage("\"[a-zA-Z0-9\\-\\_\\.]+\""));
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
    @MethodSource("storageProvider")
    void testRemoteRepositoryFormInvalidImplementation(String implementation,
                                                       int numErrors,
                                                       String errorMessage)
    {
        // given
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(ID_VALID);
        repositoryForm.setPolicy(POLICY_VALID.getPolicy());
        repositoryForm.setStorageProvider(implementation);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
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
        repositoryForm.setStorageProvider(FileSystemStorageProvider.ALIAS);
        repositoryForm.setLayout(LAYOUT_VALID);
        repositoryForm.setType(TYPE_VALID.getType());
        repositoryForm.setStatus(STATUS_VALID.getStatus());
        repositoryForm.setProxyConfiguration(proxyConfigurationForm);
        repositoryForm.setRemoteRepository(remoteRepositoryForm);
        repositoryForm.setHttpConnectionPool(HTTP_CONNECTION_POOL_INVALID);
        repositoryForm.setRepositoryConfiguration(repositoryConfiguration);

        validateAndAssert(repositoryForm, 1, "The httpConnectionPool value must be greater, or equal to zero.");
    }

    private String getPatternLocalisedMessage(String expectedPattern)
    {
        String message;
        try
        {
            final String translatedMessage = new PlatformResourceBundleLocator("org.hibernate.validator.ValidationMessages")
                                                                 .getResourceBundle(Locale.getDefault())
                                                                 .getString("javax.validation.constraints.Pattern.message");
            message = translatedMessage.replace("\"{regexp}\"", expectedPattern);
        }
        catch (MissingResourceException e)
        {
            message = "must match ";
        }
        
        return message;
    }

}
