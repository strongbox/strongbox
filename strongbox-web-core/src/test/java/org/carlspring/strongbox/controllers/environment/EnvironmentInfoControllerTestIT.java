package org.carlspring.strongbox.controllers.environment;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class EnvironmentInfoControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/environment/info");
    }

    @Test
    void testGetEnvironmentInfo()
            throws Exception
    {
        String url = getContextBaseUrl();

        String envInfo = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                .when()
                                .get(url)
                                .prettyPeek()
                                .asString();

        Map<String, List<?>> returnedMap = objectMapper.readValue(envInfo,
                                                                  new TypeReference<Map<String, List<?>>>()
                                                                  {
                                                                  });

        assertThat(returnedMap).as("Failed to get all environment info list!").isNotNull();

        List<?> environmentVariables = returnedMap.get("environment");

        assertThat(environmentVariables).as("Failed to get environment variables list!").isNotNull();
        assertThat(environmentVariables).as("Returned environment variables are empty").isNotEmpty();

        List<?> systemProperties = returnedMap.get("system");

        assertThat(systemProperties).as("Failed to get system properties list!").isNotNull();
        assertThat(systemProperties).as("Returned system properties are empty").isNotEmpty();

        List<?> jvmArguments = returnedMap.get("jvm");

        assertThat(jvmArguments).as("Failed to get JVM arguments list!").isNotNull();
        assertThat(jvmArguments).as("Returned JVM arguments are not empty").isEmpty();

        List<?> strongboxInfo = returnedMap.get("strongbox");

        assertThat(strongboxInfo).as("Failed to get strongbox info list!").isNotNull();
        assertThat(strongboxInfo).as("Returned strongbox info are empty").isNotEmpty();
    }

    @Test
    void testGetEnvironmentInfoCheckSorted()
            throws Exception
    {
        String url = getContextBaseUrl();

        String envInfo = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                .when()
                                .get(url)
                                .asString();

        JsonNode root = objectMapper.readTree(envInfo);

        // Environment variables
        JsonNode environmentNode = root.path("environment");
        ObjectReader listEnvironmentInfoReader = objectMapper.readerFor(new TypeReference<List<EnvironmentInfo>>()
        {
        });

        List<EnvironmentInfo> environmentVariables = listEnvironmentInfoReader.readValue(environmentNode);
        Comparator<EnvironmentInfo> environmentInfoComparator = Comparator.comparing(EnvironmentInfo::getName,
                                                                                     String.CASE_INSENSITIVE_ORDER);
        List<EnvironmentInfo> sortedEnvironmentVariables = new ArrayList<>(environmentVariables);
        sortedEnvironmentVariables.sort(environmentInfoComparator);

        assertThat(environmentVariables).as("Failed to get environment variables list!").isNotNull();
        assertThat(sortedEnvironmentVariables)
                .as("Environment variables list is not sorted!")
                .isEqualTo(environmentVariables);

        // System properties
        JsonNode systemNode = root.path("system");
        List<EnvironmentInfo> systemProperties = listEnvironmentInfoReader.readValue(systemNode);
        List<EnvironmentInfo> sortedSystemProperties = new ArrayList<>(systemProperties);
        sortedSystemProperties.sort(environmentInfoComparator);

        assertThat(systemProperties).as("Failed to get system properties list!").isNotNull();
        assertThat(sortedSystemProperties)
                .as("System properties list is not sorted!")
                .isEqualTo(systemProperties);

        // JVM arguments
        JsonNode jvmNode = root.path("jvm");
        ObjectReader listStringReader = objectMapper.readerFor(new TypeReference<List<String>>()
        {
        });
        List<String> jvmArguments = listStringReader.readValue(jvmNode);
        Comparator<String> stringComparator = String::compareToIgnoreCase;
        List<String> sortedJvmArguments = new ArrayList<>(jvmArguments);
        sortedJvmArguments.sort(stringComparator);

        assertThat(jvmArguments).as("Failed to get JVM arguments list!").isNotNull();
        assertThat(sortedJvmArguments)
                .as("JVM arguments list is not sorted!")
                .isEqualTo(jvmArguments);
    }

}
