package org.carlspring.strongbox.util;

import org.carlspring.strongbox.resource.ResourceCloser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class MessageDigestUtils
{

    private static final Logger logger = LoggerFactory.getLogger(MessageDigestUtils.class);


    public static String convertToHexadecimalString(MessageDigest md)
    {
        byte[] hash = md.digest();
        StringBuilder sb = new StringBuilder(2 * hash.length);
        for (byte b : hash)
        {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }

    public static void writeDigestAsHexadecimalString(MessageDigest digest,
                                                      File artifactFile,
                                                      String checksumFileExtension)
            throws IOException
    {
        String checksum = MessageDigestUtils.convertToHexadecimalString(digest);

        writeChecksum(artifactFile, checksumFileExtension, checksum);
    }

    public static void writeChecksum(File artifactFile, String checksumFileExtension, String checksum)
            throws IOException
    {
        final File checksumFile = new File(artifactFile.getAbsolutePath() + checksumFileExtension);

        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(checksumFile);

            fos.write((checksum + "\n").getBytes());
            fos.flush();
            fos.close();
        }
        finally
        {
            ResourceCloser.close(fos, logger);
        }
    }

    public static String readChecksumFile(String path)
            throws IOException
    {
        InputStream is = null;

        try
        {
            is = new FileInputStream(path);

            return readChecksumFile(is);
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    public static String readChecksumFile(InputStream is)
            throws IOException
    {
        BufferedReader br = null;

        try
        {
            br = new BufferedReader(new InputStreamReader(is));

            return br.readLine();
        }
        finally
        {
            ResourceCloser.close(br, null);
            ResourceCloser.close(is, null);
        }
    }

}
