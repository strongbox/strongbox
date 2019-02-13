package org.carlspring.strongbox.controllers.environment;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@SpringBootTest
public class EnvironmentInfoControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private ObjectMapper mapper;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    void testGetEnvironmentInfo()
            throws Exception
    {
        String path = "/api/configuration/environment/info";

        String envInfo = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                .when()
                                .get(path)
                                .prettyPeek()
                                .asString();

        Map<String, List<?>> returnedMap = mapper.readValue(envInfo,
                                                            new TypeReference<Map<String, List<?>>>()
                                                            {
                                                            });

        assertNotNull(returnedMap, "Failed to get all environment info list!");

        List<?> environmentVariables = returnedMap.get("environment");

        assertNotNull(environmentVariables, "Failed to get environment variables list!");
        assertFalse(environmentVariables.isEmpty(), "Returned environment variables are empty");

        List<?> systemProperties = returnedMap.get("system");

        assertNotNull(systemProperties, "Failed to get system properties list!");
        assertFalse(systemProperties.isEmpty(), "Returned system properties are empty");

        List<?> jvmArguments = returnedMap.get("jvm");

        assertNotNull(jvmArguments, "Failed to get JVM arguments list!");
        assertTrue(jvmArguments.isEmpty(), "Returned JVM arguments are not empty");
    }

    @Test
    void testGetEnvironmentInfoCheckSorted()
            throws Exception
    {
        String path = "/api/configuration/environment/info";

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

        assertNotNull(environmentVariables, "Failed to get environment variables list!");
        assertEquals(environmentVariables, sortedEnvironmentVariables, "Environment variables list is not sorted!");

        // System properties
        JsonNode systemNode = root.path("system");
        List<EnvironmentInfo> systemProperties = listEnvironmentInfoReader.readValue(systemNode);
        List<EnvironmentInfo> sortedSystemProperties = new ArrayList<>(systemProperties);
        sortedSystemProperties.sort(environmentInfoComparator);

        assertNotNull(systemProperties, "Failed to get system properties list!");
        assertEquals(systemProperties, sortedSystemProperties, "System properties list is not sorted!");

        // JVM arguments
        JsonNode jvmNode = root.path("jvm");
        ObjectReader listStringReader = mapper.readerFor(new TypeReference<List<String>>()
        {
        });
        List<String> jvmArguments = listStringReader.readValue(jvmNode);
        Comparator<String> stringComparator = String::compareToIgnoreCase;
        List<String> sortedJvmArguments = new ArrayList<>(jvmArguments);
        sortedJvmArguments.sort(stringComparator);

        assertNotNull(jvmArguments, "Failed to get JVM arguments list!");
        assertEquals(jvmArguments, sortedJvmArguments, "JVM arguments list is not sorted!");
    }

}
