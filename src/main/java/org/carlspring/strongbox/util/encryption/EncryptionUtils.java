
package org.carlspring.strongbox.util.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author Martin Todorov
 * @email carlspring@gmail.com
 */
public class EncryptionUtils
{

    private static Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);

    public static final String MD5 = "MD5";


    /**
     * Encrypts a String using the MD5 algorithm.
     *
     * @param password
     * @return
     */
    public static String encryptWithMD5(String password)
    {
        StringBuffer hexString = new StringBuffer();
        byte[] defaultBytes = password.getBytes();
        try
        {
            MessageDigest algorithm = MessageDigest.getInstance(MD5);
            algorithm.reset();
            algorithm.update(defaultBytes);
            byte messageDigest[] = algorithm.digest();

            for (byte digestByte : messageDigest)
            {
                String hex = Integer.toHexString(0xFF & digestByte);
                if (hex.length() == 1)
                    hexString.append('0');

                hexString.append(hex);
            }
        }
        catch (NoSuchAlgorithmException nsae)
        {
            logger.error(nsae.getMessage(), nsae);
        }
        return hexString.toString();
    }

    public static String generateRandomKey()
    {
        StringBuilder key = new StringBuilder();

        Random randomNumber = new Random();

        for (int i = 0; i < 15;)
        {
            int value = randomNumber.nextInt(125);
            if (value == 33 || (value >= 35 && value <= 122) &&
                value != 39 && // '
                value != 63 && // ?
                value != 96 )  // `
            {
                key.append((char) value);
                i++;
            }
        }

        return key.toString();
    }

    public static String generateKeyHash(String password, String randomKey)
    {
        return encryptWithMD5(password+randomKey);
    }

}
