package org.carlspring.strongbox.io;

import java.io.*;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
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
        try
        {
            assertTrue(artifactInputStream.getFileExtension(IOUtils.toByteArray(inputStream)).equalsIgnoreCase(".txt"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}

