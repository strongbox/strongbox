package org.carlspring.strongbox.util;


import org.carlspring.strongbox.config.PypiLayoutProviderTestConfig;
import org.carlspring.strongbox.domain.PypiPackageInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.validation.ConstraintViolationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = PypiLayoutProviderTestConfig.class)
public class PypiMetadataParserTest
{

    @Autowired
    PypiMetadataParser pypiMetadataParser;

    @Autowired
    PypiPackageInfoValidator pypiPackageInfoValidator;

    @Test
    public void testParseFileToPypiMetadataDto()
            throws IllegalAccessException, IOException
    {
        PypiPackageInfo testDao = pypiMetadataParser.parseMetadataFile(
                new FileInputStream("src/test/resources/org.carlspring.strongbox.util/PKG-INFO"));

        assertThat("hello-strongbox-pip").isEqualTo(testDao.getName());
        assertThat("1.0").isEqualTo(testDao.getMetadataVersion().getVersionString());
        assertThat("1.0.0").isEqualTo(testDao.getVersion());
        assertThat("Hello, Strongbox [pip]!").isEqualTo(testDao.getSummary());
        assertThat("https://github.com/strongbox/strongbox-examples").isEqualTo(testDao.getHomePage());
        assertThat("Martin Todorov").isEqualTo(testDao.getAuthor());
        assertThat("foo@bar.com").isEqualTo(testDao.getAuthorEmail());
        assertThat("Apache 2.0").isEqualTo(testDao.getLicense());
        assertThat("UNKNOWN").isEqualTo(testDao.getDescriptionContentType());
        assertThat("UNKNOWN").isEqualTo(testDao.getDescription());
        assertThat("UNKNOWN").isEqualTo(testDao.getPlatform());
    }

    @Test
    public void testParseFileWithEmptyValuesToPypiMetadataDto()
    {
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> pypiMetadataParser.parseMetadataFile(
                        new FileInputStream("src/test/resources/org.carlspring.strongbox.util/PKG-INFO-empty-values")));
    }

    @Test
    public void testParseFileWithInvalidMetadataVersion()
    {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pypiMetadataParser.parseMetadataFile(new FileInputStream(
                        "src/test/resources/org.carlspring.strongbox.util/PKG-INFO-invalid-metadata-version")));
    }

    @Test
    public void testParseFileWithInvalidName()
    {
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> pypiMetadataParser.parseMetadataFile(
                        new FileInputStream("src/test/resources/org.carlspring.strongbox.util/PKG-INFO-invalid-name")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testNullAndEmptyVersion(String version)
    {
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> validate(prepareKeyValueMap(version)));
    }

    private Map<String, String> prepareKeyValueMap(String version)
    {
        Map<String, String> keyValueMap = new HashMap<>();

        // prepare test data to test version field validation
        // set valid default values for all other fields
        keyValueMap.put("Version", version);
        keyValueMap.put("Metadata-Version", "1.0");
        keyValueMap.put("Name", "hello-strongbox-pip");
        keyValueMap.put("Summary", "Hello, Strongbox [pip]");
        keyValueMap.put("Home-page", "https://github.com/strongbox/strongbox-examples");
        keyValueMap.put("Author", "Martin Todorov");
        keyValueMap.put("Author-email", "foo@bar.com");
        keyValueMap.put("License", "Apache 2.0");
        keyValueMap.put("Description-Content-Type", "UNKNOWN");
        keyValueMap.put("Description", "UNKNOWN");
        keyValueMap.put("Platform", "UNKNOWN");
        return keyValueMap;
    }

    private void validate(Map<String, String> keyValueMap)
            throws IllegalAccessException, ConstraintViolationException
    {
        PypiPackageInfo packageInfo = pypiMetadataParser.populateAnnotatedFields(new PypiPackageInfo(), keyValueMap);
        pypiPackageInfoValidator.validate(packageInfo);
    }

}
