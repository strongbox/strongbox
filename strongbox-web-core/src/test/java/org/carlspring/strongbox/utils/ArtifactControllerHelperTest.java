package org.carlspring.strongbox.utils;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ArtifactControllerHelperTest
{

    @Test
    public void testGetDirs()
            throws Exception
    {
        File file = Mockito.mock(File.class);
        File[] t = { getDirectory("c"),
                     getDirectory("a"),
                     getFile("a"),
                     getDirectory("b") };
        when(file.listFiles()).thenReturn(t);

        List<File> files = ArtifactControllerHelper.getDirectories(file);

        assertEquals(3, files.size());
        assertTrue(files.get(0).getName().equals("a"));
        assertTrue(files.get(1).getName().equals("b"));
        assertTrue(files.get(2).getName().equals("c"));
    }

    @Test
    public void testGetFiles()
            throws Exception
    {
        File file = Mockito.mock(File.class);
        File[] t = { getFile("c"),
                     getFile("a"),
                     getDirectory("a"),
                     getFile("b") };
        when(file.listFiles()).thenReturn(t);

        List<File> files = ArtifactControllerHelper.getFiles(file);

        assertEquals(3, files.size());
        assertTrue(files.get(0).getName().equals("a"));
        assertTrue(files.get(1).getName().equals("b"));
        assertTrue(files.get(2).getName().equals("c"));
    }

    private File getFile(String fileName)
    {
        File file = Mockito.mock(File.class);
        when(file.isFile()).thenReturn(true);
        when(file.getPath()).thenReturn(fileName);
        when(file.getName()).thenReturn(fileName);

        return file;
    }

    private File getDirectory(String fileName)
    {
        File file = Mockito.mock(File.class);
        when(file.isFile()).thenReturn(false);
        when(file.isDirectory()).thenReturn(true);
        when(file.getPath()).thenReturn(fileName);
        when(file.getName()).thenReturn(fileName);

        return file;
    }


}
