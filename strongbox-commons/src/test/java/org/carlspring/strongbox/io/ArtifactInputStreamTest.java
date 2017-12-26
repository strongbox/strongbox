package org.carlspring.strongbox.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ArtifactInputStreamTest
{


    ArtifactInputStream artifactInputStream ;
    InputStream inputStream;

    @Before
    public void setUp()
            throws FileNotFoundException, NoSuchAlgorithmException
    {
        String path = "src/test/resources/testTextFile.txt";
        inputStream = new BufferedInputStream(new FileInputStream(path.trim()));
        artifactInputStream = new ArtifactInputStream(null, inputStream){};
    }


    @Test
    public void testFileExtensionEqualsTxt()
    {
        assertTrue(artifactInputStream.getFileExtension().equalsIgnoreCase(".txt"));
    }

}
