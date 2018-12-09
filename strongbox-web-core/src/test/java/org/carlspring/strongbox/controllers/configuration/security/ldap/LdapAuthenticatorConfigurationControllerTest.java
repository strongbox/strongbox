package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.ConfigurableProviderManager;
import org.carlspring.strongbox.authentication.api.ldap.LdapAuthenticationConfigurationManager;
import org.carlspring.strongbox.authentication.api.ldap.LdapConfiguration;
import org.carlspring.strongbox.authentication.registry.AuthenticationResourceManager;
import org.carlspring.strongbox.authentication.support.ExternalRoleMapping;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @author sbespalov
 */
@IntegrationTest
@ActiveProfiles({ "LdapAuthenticatorConfigurationControllerTest",
                  "test" })
@Execution(SAME_THREAD)
public class LdapAuthenticatorConfigurationControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private ConfigurableProviderManager providerManager;

    @Inject
    private LdapAuthenticationConfigurationManager ldapAuthenticationConfigurationManager;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        setContextBaseUrl("/api/configuration/ldap");
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldReturnProperLdapConfiguration()
    {
        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .body("url", startsWith("ldap://127.0.0.1"))
               .body("groupSearchBase", equalTo("ou=Users"))
               .body("groupSearchFilter", equalTo("(uid={0})"))
               .body("roleMappingList[0].externalRole", equalTo("Developers"))
               .body("roleMappingList[0].strongboxRole", equalTo("REPOSITORY_MANAGER"))
               .body("roleMappingList[1].externalRole", equalTo("Contributors"))
               .body("roleMappingList[1].strongboxRole", equalTo("USER_ROLE"))
               .body("userDnPatternList[0]", equalTo("uid={0},ou=Users"))
               .body("authorities.groupSearchBase", equalTo("ou=Groups"))
               .body("authorities.groupSearchFilter", equalTo("(uniqueMember={0})"))
               .body("authorities.searchSubtree", equalTo(true))
               .body("authorities.groupRoleAttribute", equalTo("cn"))
               .body("authorities.rolePrefix", equalTo(""))
               .body("authorities.convertToUpperCase", equalTo(false))
               .statusCode(HttpStatus.OK.value());
    }


    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateFullLdapConfiguration()
            throws IOException
    {

        LdapConfiguration configuration = ldapAuthenticationConfigurationManager.getConfiguration();

        configuration.getGroupSearch().setGroupSearchBase("ou=People");
        configuration.getGroupSearch().setGroupSearchFilter("(people={0})");

        configuration.getRoleMappingList().add(new ExternalRoleMapping("ArtifactsManager", "ARTIFACTS_MANAGER"));
        configuration.getRoleMappingList().add(new ExternalRoleMapping("LogsManager", "LOGS_MANAGER"));

        configuration.getUserDnPatternList().add("uid={0},ou=AllUsers");
        configuration.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(configuration)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .body("url", startsWith("ldap://127.0.0.1"))
               .body("groupSearchBase", equalTo("ou=People"))
               .body("groupSearchFilter", equalTo("(people={0})"))
               .body("authorities.groupSearchBase", equalTo("ou=Groups"))
               .body("authorities.groupSearchFilter", equalTo("(uniqueMember={0})"))
               .body("authorities.searchSubtree", equalTo(true))
               .body("authorities.groupRoleAttribute", equalTo("cn"))
               .body("authorities.rolePrefix", equalTo(""))
               .body("authorities.convertToUpperCase", equalTo(false))
               .body("roleMappingList[0].externalRole", equalTo("Developers"))
               .body("roleMappingList[0].strongboxRole", equalTo("REPOSITORY_MANAGER"))
               .body("roleMappingList[1].externalRole", equalTo("Contributors"))
               .body("roleMappingList[1].strongboxRole", equalTo("USER_ROLE"))
               .body("roleMappingList[2].externalRole", equalTo("ArtifactsManager"))
               .body("roleMappingList[2].strongboxRole", equalTo("ARTIFACTS_MANAGER"))
               .body("roleMappingList[3].externalRole", equalTo("LogsManager"))
               .body("roleMappingList[3].strongboxRole", equalTo("LOGS_MANAGER"))
               .body("userDnPatternList[0]", equalTo("uid={0},ou=Users"))
               .body("userDnPatternList[1]", equalTo("uid={0},ou=AllUsers"))
               .statusCode(HttpStatus.OK.value());

        // rollback
        providerManager.reload();
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestRequiresUrl()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("username");
        form.setPassword("password");

        form.getConfiguration().setUrl(null);

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("errors[0]['name']", equalTo("configuration.url"))
               .body("errors[0]['messages'][0]", equalTo("must not be empty"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestRequiresValidUrl()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("username");
        form.setPassword("password");

        form.getConfiguration().setUrl("http://host:port?thisIsWrongUrl=true");

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("errors[0]['name']", equalTo("configuration.url"))
               .body("errors[0]['messages'][0]", equalTo("must be a valid URI"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFailWithInvalidConfiguration()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("mtodorov");
        form.setPassword("password");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapConfiguration subform = form.getConfiguration();
        subform.setUserDnPatternList(userDnPatterns);
        subform.getGroupSearch().setGroupSearchBase("ou=Employee");
        subform.getGroupSearch().setGroupSearchFilter("(employee={0})");

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    @Disabled
    public void ldapConfigurationTestShouldFailWithWithInvalidManagerDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration().setManagerDn("uid=unknown,ou=system");
        form.getConfiguration().setManagerPassword("secret");

        form.setUsername("mtodorov");
        form.setPassword("password");

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
               .body("message", equalTo("Failed to test LDAP configuration."));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFailWithInvalidUserDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("daddy");
        form.setPassword("mummy");

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldPassWithValidUserDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("mtodorov");
        form.setPassword("password");

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldNotAffectInternalConfiguration()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();

        form.setUsername("mtodorov");
        form.setPassword("password");

        form.getConfiguration()
            .setRoleMappingList(Stream.of(new ExternalRoleMapping("ArtifactsManager", "ARTIFACTS_MANAGER"),
                                          new ExternalRoleMapping("LogsManager", "LOGS_MANAGER"))
                                      .collect(Collectors.toList()));

        String url = getContextBaseUrl() + "/test";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));

        shouldReturnProperLdapConfiguration();
    }

    private LdapConfigurationTestForm getLdapConfigurationTestForm()
    {
        LdapConfigurationTestForm form = new LdapConfigurationTestForm();

        LdapConfiguration configuration = ldapAuthenticationConfigurationManager.getConfiguration();

        form.setConfiguration(configuration);
        return form;
    }

    @Configuration
    @Profile("LdapAuthenticatorConfigurationControllerTest")
    @Import(HazelcastConfiguration.class)
    @ImportResource("classpath:/ldapServerApplicationContext.xml")
    public static class LdapAuthenticatorConfigurationControllerTestConfiguration
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdLacct()
        {
            return new HazelcastInstanceId("LdapAuthenticatorConfigurationControllerTest-hazelcast-instance");
        }

        @Bean
        @Primary
        public AuthenticationResourceManager testAuthenticationResourceManager()
        {
            return new TestAuthenticationResourceManager();
        }

    }

    private static class TestAuthenticationResourceManager
            extends AuthenticationResourceManager
    {

        @Override
        public Resource getAuthenticationPropertiesResource()
        {
            return new DefaultResourceLoader().getResource("classpath:ldap-strongbox-authentication-providers.yaml");
        }

    }

}
