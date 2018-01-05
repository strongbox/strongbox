package org.carlspring.strongbox.controllers.environment;

import org.carlspring.strongbox.controllers.BaseController;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pablo Tirado
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/configuration/environment/info")
@Api("/configuration/environment/info")
public class EnvironmentInfoController
        extends BaseController
{

    private ObjectMapper objectMapper;

    public EnvironmentInfoController(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    @ApiOperation(value = "List all the environment variables and system properties.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getEnvironmentAndSystemProperties()
    {
        logger.debug("Listing of all environment variables and system properties");

        Map<String, List<EnvironmentInfo>> propertiesMap = new HashMap<>();
        List<EnvironmentInfo> environmentVariables = getEnvironmentVariables();
        List<EnvironmentInfo> systemProperties = getSystemProperties();

        propertiesMap.put("environment", environmentVariables);
        propertiesMap.put("system", systemProperties);

        try
        {
            return ResponseEntity.ok(objectMapper.writeValueAsString(propertiesMap));
        }
        catch (JsonProcessingException e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("{ 'error': '%s' }", e.getMessage()));
        }
    }

    private List<EnvironmentInfo> getEnvironmentVariables()
    {
        Map<String, String> environmentMap = System.getenv();

        return environmentMap.entrySet().stream()
                             .sorted(Map.Entry.comparingByKey())
                             .map(e -> new EnvironmentInfo(e.getKey(), e.getValue()))
                             .collect(Collectors.toList());
    }

    private List<EnvironmentInfo> getSystemProperties()
    {
        Properties systemProperties = System.getProperties();

        return systemProperties.entrySet().stream()
                               .sorted(Comparator.comparing(
                                       Map.Entry::getKey,
                                       Comparator.comparing(e -> (String) e))
                               )
                               .map(e -> new EnvironmentInfo((String) e.getKey(), (String) e.getValue()))
                               .collect(Collectors.toList());
    }
}
