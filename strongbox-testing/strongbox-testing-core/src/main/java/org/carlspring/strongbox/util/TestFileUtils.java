package org.carlspring.strongbox.util;

import org.carlspring.commons.io.RandomInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author mtodorov
 */
public class TestFileUtils
{

    private TestFileUtils()
    {
    }

    public static void deleteIfExists(File file)
    {
        if (file.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static void generateFile(ZipOutputStream zos,
                                    long size)
            throws IOException
    {
        generateFile(zos, size, "file-with-given-size");
    }

    public static void generateFile(ZipOutputStream zos,
                                    long size,
                                    String name)
            throws IOException
    {
        ZipEntry ze = new ZipEntry(name);
        zos.putNextEntry(ze);

        RandomInputStream ris = new RandomInputStream(size);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = ris.read(buffer)) > 0)
        {
            zos.write(buffer, 0, len);
        }

        ris.close();
        zos.closeEntry();
    }

    public static void generateFile(BufferedOutputStream bos,
                                    long size)
            throws IOException
    {
        bos.write("data = \"".getBytes(StandardCharsets.UTF_8));

        OutputStream dataOut = Base64.getEncoder().wrap(bos);
        RandomInputStream ris = new RandomInputStream(size);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = ris.read(buffer)) > 0)
        {
            dataOut.write(buffer, 0, len);
        }
        ris.close();

        bos.write("\";".getBytes(StandardCharsets.UTF_8));
    }
}
