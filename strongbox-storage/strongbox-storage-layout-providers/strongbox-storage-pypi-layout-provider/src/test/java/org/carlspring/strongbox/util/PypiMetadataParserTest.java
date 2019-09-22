package org.carlspring.strongbox.util;


import org.carlspring.strongbox.config.PypiMetadataFileParserTestConfig;
import org.carlspring.strongbox.domain.PypiPackageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@ContextConfiguration(classes = { PypiMetadataFileParserTestConfig.class })
@Execution(CONCURRENT)
public class PypiMetadataParserTest
{

    @Test
    public void testParseFileToPypiMetadataDto()
            throws IllegalAccessException, IOException
    {
        PypiMetadataParser pypiMetadataParser = new PypiMetadataParser();
        PypiPackageInfo testDao = pypiMetadataParser.parseMetadataFile(new FileInputStream("src/test/resources/org.carlspring.strongbox.util/metadata.xml"));

        assertThat("hello-strongbox-pip").isEqualTo(testDao.getName());
        assertThat("1.0").isEqualTo(testDao.getMetadataVersion());
        assertThat("1.0.0").isEqualTo(testDao.getVersion());
        assertThat( "Hello, Strongbox [pip]!").isEqualTo(testDao.getSummary());
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
            throws IllegalAccessException, IOException
    {
        PypiMetadataParser pypiMetadataParser = new PypiMetadataParser();
        PypiPackageInfo testDao = pypiMetadataParser.parseMetadataFile(new FileInputStream("src/test/resources/org.carlspring.strongbox.util/metadata_empty_values.xml"));

        assertThat(testDao.getName().isEmpty()).isTrue();
        assertThat(testDao.getMetadataVersion().isEmpty()).isTrue();
        assertThat(testDao.getVersion().isEmpty()).isTrue();
        assertThat(testDao.getSummary().isEmpty()).isTrue();
        assertThat(testDao.getHomePage().isEmpty()).isTrue();
        assertThat(testDao.getAuthor().isEmpty()).isTrue();
        assertThat(testDao.getAuthorEmail().isEmpty()).isTrue();
        assertThat(testDao.getLicense().isEmpty()).isTrue();
        assertThat(testDao.getDescriptionContentType().isEmpty()).isTrue();
        assertThat(testDao.getDescription().isEmpty()).isTrue();
        assertThat(testDao.getPlatform().isEmpty()).isTrue();
    }

}
