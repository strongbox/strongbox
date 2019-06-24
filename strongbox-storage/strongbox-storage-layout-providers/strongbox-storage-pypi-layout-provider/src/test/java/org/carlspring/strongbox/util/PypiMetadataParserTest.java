package org.carlspring.strongbox.util;


import org.carlspring.strongbox.config.PypiMetadataFileParserTestConfig;
import org.carlspring.strongbox.domain.PypiPackageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        assertEquals(testDao.getName(), "hello-strongbox-pip");
        assertEquals(testDao.getMetadataVersion(), "1.0");
        assertEquals(testDao.getVersion(), "1.0.0");
        assertEquals(testDao.getSummary(), "Hello, Strongbox [pip]!");
        assertEquals(testDao.getHomePage(), "https://github.com/strongbox/strongbox-examples");
        assertEquals(testDao.getAuthor(), "Martin Todorov");
        assertEquals(testDao.getAuthorEmail(), "foo@bar.com");
        assertEquals(testDao.getLicense(), "Apache 2.0");
        assertEquals(testDao.getDescriptionContentType(), "UNKNOWN");
        assertEquals(testDao.getDescription(), "UNKNOWN");
        assertEquals(testDao.getPlatform(), "UNKNOWN");
    }

    @Test
    public void testParseFileWithEmptyValuesToPypiMetadataDto()
            throws IllegalAccessException, IOException
    {
        PypiMetadataParser pypiMetadataParser = new PypiMetadataParser();
        PypiPackageInfo testDao = pypiMetadataParser.parseMetadataFile(new FileInputStream("src/test/resources/org.carlspring.strongbox.util/metadata_empty_values.xml"));

        assertTrue(testDao.getName().isEmpty());
        assertTrue(testDao.getMetadataVersion().isEmpty());
        assertTrue(testDao.getVersion().isEmpty());
        assertTrue(testDao.getSummary().isEmpty());
        assertTrue(testDao.getHomePage().isEmpty());
        assertTrue(testDao.getAuthor().isEmpty());
        assertTrue(testDao.getAuthorEmail().isEmpty());
        assertTrue(testDao.getLicense().isEmpty());
        assertTrue(testDao.getDescriptionContentType().isEmpty());
        assertTrue(testDao.getDescription().isEmpty());
        assertTrue(testDao.getPlatform().isEmpty());
    }

}
