
package org.carlspring.strongbox.security.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Todorov
 * @email carlspring@gmail.com
 */
public class EncryptionUtils
{

    private static Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);

    private EncryptionUtils() 
    {
    }


    /**
     * Encrypts a String using the MD5 algorithm.
     *
     * @param password
     * @return
     */
    public static String encryptWithMD5(String password)
    {
        StringBuilder hexString = new StringBuilder();
        byte[] defaultBytes = password.getBytes();

        try
        {
            MessageDigest algorithm = MessageDigest.getInstance(EncryptionAlgorithms.MD5.toString());
            algorithm.reset();
            algorithm.update(defaultBytes);
            byte[] messageDigest = algorithm.digest();

            for (byte digestByte : messageDigest)
            {
                String hex = Integer.toHexString(0xFF & digestByte);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }

                hexString.append(hex);
            }
        }
        catch (NoSuchAlgorithmException nsae)
        {
            logger.error(nsae.getMessage(), nsae);
        }

        return hexString.toString();
    }

}
