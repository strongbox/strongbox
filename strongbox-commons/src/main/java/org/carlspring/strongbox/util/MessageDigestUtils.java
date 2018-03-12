package org.carlspring.strongbox.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class MessageDigestUtils
{

    private static final Logger logger = LoggerFactory.getLogger(MessageDigestUtils.class);

    private MessageDigestUtils() 
    {
    }


    public static String convertToHexadecimalString(MessageDigest md)
    {
        byte[] hash = md.digest();
        return convertToHexadecimalString(hash);
    }


    public static String convertToHexadecimalString(byte[] hash)
    {
        StringBuilder sb = new StringBuilder(2 * hash.length);
        for (byte b : hash)
        {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }

    public static void writeDigestAsHexadecimalString(MessageDigest digest,
                                                      Path artifactPath,
                                                      String checksumFileExtension)
            throws IOException
    {
        String checksum = MessageDigestUtils.convertToHexadecimalString(digest);

        writeChecksum(artifactPath, checksumFileExtension, checksum);
    }

    public static void writeChecksum(Path artifactPath, String checksumFileExtension, String checksum)
            throws IOException
    {
        final Path checksumPath = artifactPath.resolveSibling(artifactPath.getFileName() + checksumFileExtension);
        try (OutputStream fos = Files.newOutputStream(checksumPath))
        {
            fos.write((checksum).getBytes());
            fos.flush();
        }
    }

    public static String readChecksumFile(String path)
            throws IOException
    {
        try (InputStream is = new FileInputStream(path))
        {
            return readChecksumFile(is);
        }
    }

    public static String readChecksumFile(InputStream is)
            throws IOException
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
        {
            return br.readLine();
        }
    }

}
