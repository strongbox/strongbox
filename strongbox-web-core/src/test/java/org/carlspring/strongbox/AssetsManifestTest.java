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
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases which check if UI assets are accessible.
 *
 * @author Pablo Tirado
 */
@IntegrationTest
public class AssetsManifestTest
{

    private static String fileResource = "assets-manifest.json";
    private static String rootPath = "/";
    private static Pattern styleRegex = Pattern.compile("\\/static\\/assets\\/styles(.+).css");
    private static Pattern runtimeRegex = Pattern.compile("\\/static\\/assets\\/runtime(.+).js");
    private static Pattern polyfillsRegex = Pattern.compile("\\/static\\/assets\\/polyfills(.+).js");
    private static Pattern mainRegex = Pattern.compile("\\/static\\/assets\\/main(.+).js");
    private static List<Pattern> indexResourcePatterns;
    private static final String DATABASE_ROOT = "/database";

    @Inject
    private ObjectMapper mapper;

    @Inject
    private MockMvcRequestSpecification mockMvc;

    @BeforeAll
    static void init()
    {
        indexResourcePatterns = Lists.newArrayList();
        indexResourcePatterns.add(styleRegex);
        indexResourcePatterns.add(runtimeRegex);
        indexResourcePatterns.add(polyfillsRegex);
        indexResourcePatterns.add(mainRegex);
    }

    void changeVariables()
    {
        fileResource = "database/assets-manifest.json";
        rootPath = "/database";
        styleRegex = Pattern.compile("\\/database\\/static\\/assets\\/styles(.+).css");
        runtimeRegex = Pattern.compile("\\/database\\/static\\/assets\\/runtime(.+).js");
        polyfillsRegex = Pattern.compile("\\/database\\/static\\/assets\\/polyfills(.+).js");
        mainRegex = Pattern.compile("\\/database\\/static\\/assets\\/main(.+).js");
        indexResourcePatterns.clear();
    }

    @Test
    void webUiTest() throws IOException, URISyntaxException
    {
        testCheckAccessibleUIAssets();
    }

    @Test
    void databaseUiTest() throws IOException, URISyntaxException
    {
        changeVariables();
        init();
        testCheckAccessibleUIAssets();
    }

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
        URI manifestUri = ClassLoader.getSystemResource(fileResource).toURI();
        Path manifestPath = Paths.get(manifestUri);
        String notExistsMessage = String.format("The file \"%s\" does not exist!", fileResource);
        assertThat(Files.exists(manifestPath)).as(notExistsMessage).isTrue();

        return manifestPath;
    }

    private String checkFileContent(Path manifestPath)
            throws IOException
    {
        byte[] manifestContent = Files.readAllBytes(manifestPath);
        String manifestJsonStr = new String(manifestContent);
        String isEmptyMessage = String.format("The file \"%s\" is empty!", fileResource);
        assertThat(StringUtils.isEmpty(manifestJsonStr)).as(isEmptyMessage).isFalse();

        return manifestJsonStr;
    }

    private void checkAccessibleUIAsset(String assetFileName,
                                        String assetPath)
    {
        boolean isIndexFile = StringUtils.equals(rootPath, assetPath);

        // Add filename for root path.
        if (isIndexFile)
        {
            if (rootPath.equals(DATABASE_ROOT))
            {
                assetPath += "/" + assetFileName;
            } else
            {
                assetPath += assetFileName;
            }

        }

        MockMvcResponse response = mockMvc.get(assetPath);
        String errorMessage = String.format("The resource \"%s\" is not accessible.", assetPath);
        assertThat(HttpStatus.OK.value()).as(errorMessage).isEqualTo(response.getStatusCode());

        // Additional check for index.html
        if (isIndexFile)
        {
            checkIndexHtmlResources(response.getBody().print());
        }
    }

    private void checkIndexHtmlResources(String indexHtml)
    {
        for (Pattern pattern : indexResourcePatterns)
        {
            Matcher matcher = pattern.matcher(indexHtml);

            // Check that the resources exists in index.html.
            assertThat(matcher.find())
                    .as(String.format("The resource \"%s\" is not found in index.html.", pattern.toString())).isTrue();
        }
    }
}
