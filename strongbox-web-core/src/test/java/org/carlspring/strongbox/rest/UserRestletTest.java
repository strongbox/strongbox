package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.context.RestletTestContext;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.users.domain.User;

import javax.ws.rs.core.Response;
import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class UserRestletTest
        extends TestCaseWithArtifactGeneration
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                           "/local");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    private static RestClient client = new RestClient();

    @AfterClass
    public static void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public void testSayHello()
    {
        Response response = client.prepareTarget("/users/greet").request().get();
        RestClient.displayResponseError(response);
    }

    @Test
    public void testRetrieveUser()
            throws Exception
    {

        final String userName = "admin";

        Response response = client.prepareTarget("/users/user/" + userName).request().get();
        if (response.getStatus() != 200)
        {
            RestClient.displayResponseError(response);
            throw new Error();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String rawResponse = response.readEntity(String.class);
        System.out.println("rawResponse " + rawResponse);

        User admin = objectMapper.readValue(rawResponse, User.class);
        assertNotNull(admin);
        assertEquals(admin.getUsername(), "admin");
    }
}
