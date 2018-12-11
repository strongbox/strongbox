package org.carlspring.strongbox;

import org.carlspring.strongbox.config.IntegrationTest;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases which check if UI assets are accessible.
 *
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class AssetsManifestTest
{

    private static final String FILE_RESOURCE = "assets-manifest.json";
    private static final String ROOT_PATH = "/";
    private static final String STYLES_REGEX = "/static/assets/styles.([a-zAZ0-9]+).css";
    private static final String RUNTIME_REGEX = "/static/assets/runtime.([a-zAZ0-9]+).js";
    private static final String POLYFILLS_REGEX = "/static/assets/polyfills.([a-zAZ0-9]+).js";
    private static final String MAIN_REGEX = "/static/assets/main.([a-zAZ0-9]+).js";
    private static List<String> indexResources;

    @Inject
    private ObjectMapper mapper;

    @BeforeAll
    static void init()
    {
        indexResources = Lists.newArrayList();
        indexResources.add(STYLES_REGEX);
        indexResources.add(RUNTIME_REGEX);
        indexResources.add(POLYFILLS_REGEX);
        indexResources.add(MAIN_REGEX);
    }

    @Test
    void testCheckAccessibleUIAssets()
            throws IOException, URISyntaxException
    {
        // Read file path and check if exists.
        Path manifestPath = checkFilePath();


        // Read file content and check if empty.
        String manifestJsonStr = checkFileContent(manifestPath);


        // Convert JSON to Map.
        Map<String, String> manifestMap = mapper.readValue(manifestJsonStr,
                                                           new TypeReference<Map<String, String>>()
                                                           {
                                                           });

        // Iterate map, and check every value (asset path).
        for (Map.Entry<String, String> entry : manifestMap.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            checkAccessibleUIAsset(key, value);
        }
    }

    private Path checkFilePath()
            throws URISyntaxException
    {
        URI manifestUri = ClassLoader.getSystemResource(FILE_RESOURCE).toURI();
        Path manifestPath = Paths.get(manifestUri);
        String notExistsMessage = String.format("The file \"%s\" does not exist!", FILE_RESOURCE);
        assertTrue(Files.exists(manifestPath), notExistsMessage);

        return manifestPath;
    }

    private String checkFileContent(Path manifestPath)
            throws IOException
    {
        byte[] manifestContent = Files.readAllBytes(manifestPath);
        String manifestJsonStr = new String(manifestContent);
        String isEmptyMessage = String.format("The file \"%s\" is empty!", FILE_RESOURCE);
        assertFalse(StringUtils.isEmpty(manifestJsonStr), isEmptyMessage);

        return manifestJsonStr;
    }

    private void checkAccessibleUIAsset(String assetFileName,
                                        String assetPath)
    {
        boolean isIndexFile = StringUtils.equals(ROOT_PATH, assetPath);

        // Add filename for root path.
        if (isIndexFile)
        {
            assetPath += assetFileName;
        }

        MockMvcResponse response = given().get(assetPath);
        String errorMessage = String.format("The resource \"%s\" is not accessible.", assetPath);
        assertEquals(response.getStatusCode(), HttpStatus.OK.value(), errorMessage);

        // Additional check for index.html
        if (isIndexFile)
        {
            checkIndexHtmlResources(response.getBody().print());
        }
    }

    private void checkIndexHtmlResources(String indexHtml)
    {
        for (String indexResource : indexResources)
        {
            Pattern pattern = Pattern.compile(indexResource);
            Matcher matcher = pattern.matcher(indexHtml);

            // Check that the resources exists in index.html.
            assertTrue(matcher.find());
        }
    }
}
