package org.carlspring.strongbox.util;

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

}
