package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.ConfigurableProviderManager;
import org.carlspring.strongbox.authentication.api.AuthenticationItem;
import org.carlspring.strongbox.authentication.api.AuthenticationItems;
import org.carlspring.strongbox.authentication.api.ldap.LdapAuthenticationConfigurationManager;
import org.carlspring.strongbox.authentication.api.ldap.LdapConfiguration;
import org.carlspring.strongbox.authentication.support.ExternalRoleMapping;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.config.IntegrationTest;
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
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @author sbespalov
 */
@IntegrationTest
@ActiveProfiles({"LdapAuthenticatorConfigurationControllerTest","test"})
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
        providerManager.reload();

        AuthenticationItems authenticationItems = providerManager.getAuthenticationItems();
        List<AuthenticationItem> authenticationItemList = authenticationItems.getAuthenticationItemList();
        for (AuthenticationItem authenticationItem : authenticationItemList)
        {
            if (!"ldapUserDetailsService".equals(authenticationItem.getName()))
            {
                continue;
            }

            authenticationItem.setEnabled(true);
        }
        providerManager.updateAuthenticationItems(authenticationItems);
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldReturnProperLdapConfiguration()
    {
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .body("url", startsWith("ldap://127.0.0.1"))
               .body("userSearchBase", equalTo("ou=Users"))
               .body("userSearchFilter", equalTo("(uid={0})"))
               .body("roleMappingList[0].externalRole", equalTo("Admins"))
               .body("roleMappingList[0].strongboxRole", equalTo("ADMIN"))
               .body("roleMappingList[1].externalRole", equalTo("Developers"))
               .body("roleMappingList[1].strongboxRole", equalTo("REPOSITORY_MANAGER"))
               .body("roleMappingList[2].externalRole", equalTo("Contributors"))
               .body("roleMappingList[2].strongboxRole", equalTo("USER_ROLE"))
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

        configuration.getUserSearch().setUserSearchBase("ou=People");
        configuration.getUserSearch().setUserSearchFilter("(people={0})");

        configuration.getRoleMappingList().add(new ExternalRoleMapping("ArtifactsManager", "ARTIFACTS_MANAGER"));
        configuration.getRoleMappingList().add(new ExternalRoleMapping("LogsManager", "LOGS_MANAGER"));

        configuration.getUserDnPatternList().add("uid={0},ou=AllUsers");
        configuration.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(configuration)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .body("url", equalTo("ldap://127.0.0.1:33389/dc=carlspring,dc=com"))
               .body("userSearchBase", equalTo("ou=People"))
               .body("userSearchFilter", equalTo("(people={0})"))
               .body("authorities.groupSearchBase", equalTo("ou=Groups"))
               .body("authorities.groupSearchFilter", equalTo("(uniqueMember={0})"))
               .body("authorities.searchSubtree", equalTo(true))
               .body("authorities.groupRoleAttribute", equalTo("cn"))
               .body("authorities.rolePrefix", equalTo(""))
               .body("authorities.convertToUpperCase", equalTo(false))
               .body("roleMappingList[0].externalRole", equalTo("Admins"))
               .body("roleMappingList[0].strongboxRole", equalTo("ADMIN"))
               .body("roleMappingList[1].externalRole", equalTo("Developers"))
               .body("roleMappingList[1].strongboxRole", equalTo("REPOSITORY_MANAGER"))
               .body("roleMappingList[2].externalRole", equalTo("Contributors"))
               .body("roleMappingList[2].strongboxRole", equalTo("USER_ROLE"))
               .body("roleMappingList[3].externalRole", equalTo("ArtifactsManager"))
               .body("roleMappingList[3].strongboxRole", equalTo("ARTIFACTS_MANAGER"))
               .body("roleMappingList[4].externalRole", equalTo("LogsManager"))
               .body("roleMappingList[4].strongboxRole", equalTo("LOGS_MANAGER"))
               .body("userDnPatternList[0]", equalTo("uid={0},ou=Users"))
               .body("userDnPatternList[1]", equalTo("uid={0},ou=AllUsers"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestRequiresUrl()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("username");
        form.setPassword("password");

        form.getConfiguration().setUrl(null);

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
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
        subform.getUserSearch().setUserSearchBase("ou=Employee");
        subform.getUserSearch().setUserSearchFilter("(employee={0})");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test()
    public void ldapConfigurationTestShouldFailWithWithInvalidManagerDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration().setManagerDn("uid=unknown,ou=system");
        form.getConfiguration().setManagerPassword("secret");

        form.setUsername("mtodorov");
        form.setPassword("password");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldPassWithValidUserDnUsingMD5PasswordEncoding()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("stodorov");
        form.setPassword("password");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFailWithInValidPasswordUsingMD5PasswordEncoding()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("stodorov");
        form.setPassword("password1");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldPassWithValidUserDnUsingSHA256PasswordEncoding()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("przemyslaw.fusik");
        form.setPassword("password");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFailWithInValidPasswordUsingSHA256PasswordEncoding()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("przemyslaw.fusik");
        form.setPassword("pAssword");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldPassWithValidUserDnUsingSHA1PasswordEncoding()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("testuser1");
        form.setPassword("password");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFailWithInValidPasswordUsingSHA1PasswordEncoding()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.setUsername("testuser1");
        form.setPassword("pAsSwOrD");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
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
        form.getConfiguration().setManagerDn("uid=admin,ou=system");
        form.getConfiguration().setManagerPassword("secret");
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
        public HazelcastInstanceId hazelcastInstanceIdLacct() {
            return new HazelcastInstanceId("LdapAuthenticatorConfigurationControllerTest-hazelcast-instance");
        }

    }

}
