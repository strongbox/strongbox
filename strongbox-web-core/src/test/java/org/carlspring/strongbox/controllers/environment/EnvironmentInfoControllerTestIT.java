package org.carlspring.strongbox.controllers.environment;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class EnvironmentInfoControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private ObjectMapper mapper;

    @Test
    public void testGetEnvironmentInfo()
            throws Exception
    {
        String path = "/configuration/environment/info";

        String envInfo = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                .when()
                                .get(path)
                                .prettyPeek()
                                .asString();

        Map<String, List<?>> returnedMap = mapper.readValue(envInfo,
                                                            new TypeReference<Map<String, List<?>>>()
                                                            {
                                                            });

        assertNotNull("Failed to get all environment info list!", returnedMap);

        List<?> environmentVariables = returnedMap.get("environment");
        assertNotNull("Failed to get environment variables list!", environmentVariables);
        assertFalse("Returned environment variables are empty", environmentVariables.isEmpty());

        List<?> systemProperties = returnedMap.get("system");
        assertNotNull("Failed to get system properties list!", systemProperties);
        assertFalse("Returned system properties are empty", systemProperties.isEmpty());

        List<?> jvmArguments = returnedMap.get("jvm");
        assertNotNull("Failed to get JVM arguments list!", jvmArguments);
        assertFalse("Returned JVM arguments are empty", jvmArguments.isEmpty());
    }

    @Test
    public void testGetEnvironmentInfoCheckSorted()
            throws Exception
    {
        String path = "/configuration/environment/info";

        String envInfo = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                .when()
                                .get(path)
                                .asString();

        JsonNode root = mapper.readTree(envInfo);

        // Environment variables
        JsonNode environmentNode = root.path("environment");
        ObjectReader listEnvironmentInfoReader = mapper.readerFor(new TypeReference<List<EnvironmentInfo>>()
        {
        });
        List<EnvironmentInfo> environmentVariables = listEnvironmentInfoReader.readValue(environmentNode);
        Comparator<EnvironmentInfo> environmentInfoComparator = Comparator.comparing(EnvironmentInfo::getName,
                                                                                     String.CASE_INSENSITIVE_ORDER);
        List<EnvironmentInfo> sortedEnvironmentVariables = new ArrayList<>(environmentVariables);
        sortedEnvironmentVariables.sort(environmentInfoComparator);
        assertNotNull("Failed to get environment variables list!", environmentVariables);
        assertEquals("Environment variables list is not sorted!", environmentVariables, sortedEnvironmentVariables);

        // System properties
        JsonNode systemNode = root.path("system");
        List<EnvironmentInfo> systemProperties = listEnvironmentInfoReader.readValue(systemNode);
        List<EnvironmentInfo> sortedSystemProperties = new ArrayList<>(systemProperties);
        sortedSystemProperties.sort(environmentInfoComparator);
        assertNotNull("Failed to get system properties list!", systemProperties);
        assertEquals("System properties list is not sorted!", systemProperties, sortedSystemProperties);

        // JVM arguments
        JsonNode jvmNode = root.path("jvm");
        ObjectReader listStringReader = mapper.readerFor(new TypeReference<List<String>>()
        {
        });
        List<String> jvmArguments = listStringReader.readValue(jvmNode);
        Comparator<String> stringComparator = String::compareToIgnoreCase;
        List<String> sortedJvmArguments = new ArrayList<>(jvmArguments);
        sortedJvmArguments.sort(stringComparator);
        assertNotNull("Failed to get JVM arguments list!", jvmArguments);
        assertEquals("JVM arguments list is not sorted!", jvmArguments, sortedJvmArguments);
    }
}
