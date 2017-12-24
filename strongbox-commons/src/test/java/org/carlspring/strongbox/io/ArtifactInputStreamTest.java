package org.carlspring.strongbox.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArtifactInputStreamTest
{


    ArtifactInputStream artifactInputStream ;
    InputStream inputStream;

    @Before
    public void setUp()
            throws FileNotFoundException, NoSuchAlgorithmException
    {
        inputStream = new BufferedInputStream(new FileInputStream("src\\test\\resources\\testTextFile.txt"));
        artifactInputStream = new ArtifactInputStream(null, inputStream)
        {};

    }

    @Test
    public void testFileExtension_equals_txt(){
        assertTrue(artifactInputStream.getFileExtension().equalsIgnoreCase(".txt"));
    }

}

