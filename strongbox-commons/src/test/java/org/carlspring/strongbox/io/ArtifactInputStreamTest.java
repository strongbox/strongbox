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
    String pathTxt, pathSh;

    @Before
    public void setUp()

    {
         pathTxt = "src/test/resources/testTextFile.txt";
         pathSh = "src/test/resources/project-1.4.1.tar.gz";

   }


    @Test
    public void testFileExtensionEqualsTxt()  throws FileNotFoundException, NoSuchAlgorithmException
    {
        try
        {
            inputStream = new BufferedInputStream(new FileInputStream(pathTxt.trim()));
            artifactInputStream = new ArtifactInputStream(null, inputStream){};
            assertTrue(artifactInputStream.getFileExtension(IOUtils.toByteArray(inputStream)).equalsIgnoreCase(".txt"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testFileExtensionEqualsGz()  throws FileNotFoundException, NoSuchAlgorithmException
    {
        try
        {
            inputStream = new BufferedInputStream(new FileInputStream(pathSh.trim()));
            artifactInputStream = new ArtifactInputStream(null, inputStream){};
            assertTrue(artifactInputStream.getFileExtension(IOUtils.toByteArray(inputStream)).equalsIgnoreCase(".gz"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}

