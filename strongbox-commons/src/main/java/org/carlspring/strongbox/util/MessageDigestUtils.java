package org.carlspring.strongbox.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;

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

    public static String calculateChecksum(Path path,
                                           String type)
            throws IOException, NoSuchAlgorithmException
    {
        byte[] buffer = new byte[4096];
        MessageDigest md = MessageDigest.getInstance(type);

        try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(path), md))
        {
            while (dis.read(buffer) != -1) ;
        }

        return convertToHexadecimalString(md);
    }

    public static String calculateChecksum(Object object)
            throws IOException, NoSuchAlgorithmException
    {
        String checksum = "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            oos.writeObject(object);
            MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);
            mdos.write(baos.toByteArray());
            checksum = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        }
        return checksum;
    }
}
