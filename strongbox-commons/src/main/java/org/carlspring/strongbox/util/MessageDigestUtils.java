package org.carlspring.strongbox.util;

import org.carlspring.strongbox.security.encryption.EncryptionConstants;

import java.security.MessageDigest;

/**
 * @author mtodorov
 */
public class MessageDigestUtils
{

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

    public static String getExtensionForAlgorithm(String algorithm)
    {
        if (EncryptionConstants.ALGORITHM_MD5.equals(algorithm))
        {
            return ".md5";
        }
        if (EncryptionConstants.ALGORITHM_SHA1.equals(algorithm))
        {
            return ".sha1";
        }

        return null;
    }

}
