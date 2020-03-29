package org.carlspring.strongbox.util;


import org.carlspring.strongbox.config.PypiLayoutProviderTestConfig;
import org.carlspring.strongbox.domain.PypiPackageInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@Transactional
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

    @Test
    public void testParseFileWithInvalidPackageVersion()
    {
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> pypiMetadataParser.parseMetadataFile(
                        new FileInputStream(
                                "src/test/resources/org.carlspring.strongbox.util/PKG-INFO-invalid-version")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.1",
                             "0.2",
                             "0.3",
                             "1.0",
                             "1.1",
                             "11.5" })
    public void testValidMajorMinorVersioning(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.1.0",
                             "1.1.1",
                             "1.1.2",
                             "1.2.0",
                             "39.6.3" })
    public void testValidMajorMinorMicroVersioning(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0a1",
                             "1.0a2",
                             "1.0b1",
                             "1.0rc1",
                             "1.1a1",
                             "1.0c1",
                             "1.0c2" })
    public void testValidPreReleaseVersioning(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.post1",
                             "1.3.post2",
                             "1.0.post12" })
    public void testValidPostReleaseVersioning(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.dev1",
                             "1.0.dev2",
                             "1.0.dev0",
                             "1.0.dev4",
                             "1.4.1.dev1",
                             "26.1.dev1" })
    public void testValidDevReleaseVersioning(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2012.15",
                             "2013.1",
                             "2013.2" })
    public void testValidDateBasedVersioning(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.5+1",
                             "1.5+1.git.abc123de" })
    public void testValidLocalVersionIdentifier(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1!1.0",
                             "1!1.1",
                             "1!2.0",
                             "2!369.89" })
    public void testValidEpochVersion(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.dev456",
                             "1.0a1",
                             "1.0a2.dev456",
                             "1.0a12.dev456",
                             "1.0a12",
                             "1.0b1.dev456",
                             "1.0b2",
                             "1.0b2.post345.dev456",
                             "1.0b2.post345",
                             "1.0rc1.dev456",
                             "1.0rc1",
                             "1.0",
                             "1.0+abc.5",
                             "1.0+abc.7",
                             "1.0+5",
                             "1.0.post456.dev34",
                             "1.0.post456",
                             "1.1.dev1" })
    public void testValidVersionCombinations(String version)
    {
        assertThatCode(() -> validate(prepareKeyValueMap(version))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.dev456.post123",
                             "rc1.0a1",
                             ".1",
                             "C1.0a12.dev456",
                             "v1.0a12",
                             "1.0b1.dev456.-7",
                             "1.0b212^",
                             "1.0+abc.5+154",
                             "1.0rc+abc.7",
                             "1~0.post456",
                             "1-1-dev1",
                             "1!1.4.rc"})
    public void testInvalidVersionCombinations(String version)
    {
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> validate(prepareKeyValueMap(version)));
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
