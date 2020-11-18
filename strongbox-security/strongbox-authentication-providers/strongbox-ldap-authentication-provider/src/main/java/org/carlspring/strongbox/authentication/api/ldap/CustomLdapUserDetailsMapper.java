package org.carlspring.strongbox.authentication.api.ldap;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.util.StringUtils;

/**
 * This class handles password base64 decoding base on property: strongbox.authentication.ldap.base64EncodedPassword set to true or false.
 * If base64EncodedPassword is set to true, given string will be decoded and return decoded hash.
 *
 * <p>
 * This handles all possible cases
 * <p>
 * {ALG}md5/sha1/bcrypt(mypassword)
 * {ALG}base64.encode(md5/sha1/bcrypt(mypassword))
 * base64.encode({ALG}md5/sha1/bcrypt(mypassword))
 *
 * @author mbharti
 * @date 19/10/20
 */
public class CustomLdapUserDetailsMapper
        extends LdapUserDetailsMapper
{

    private static final String PREFIX = "{";

    private static final String SUFFIX = "}";

    private static final String EMPTY_STRING = "";

    private static final Logger logger = LoggerFactory.getLogger(CustomLdapUserDetailsMapper.class);

    private boolean isBase64EncodedPassword;

    protected String mapPassword(Object passwordValue)
    {
        String passwordValueString = super.mapPassword(passwordValue);

        if (!isBase64EncodedPassword())
        {
            return passwordValueString;
        }

        return decodeBase64EncodedPassword(passwordValueString);
    }

    private String decodeBase64EncodedPassword(String prefixEncodedPasswordString)
    {
        try
        {
            String algorithmUsed = extractId(prefixEncodedPasswordString);
            String extractBase64EncodedHash = prefixEncodedPasswordString;

            if (!StringUtils.isEmpty(algorithmUsed))
            {
                extractBase64EncodedHash = extractEncodedPassword(prefixEncodedPasswordString);

                return PREFIX + algorithmUsed + SUFFIX + decodeBase64EncodedHashWithHex(extractBase64EncodedHash);
            }
            else
            {
                return new String(Base64.getDecoder().decode(Utf8.encode(extractBase64EncodedHash)));
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to match password after decoding base64encoded hash after algorithm", e);

            return prefixEncodedPasswordString;
        }
    }

    private String decodeBase64EncodedHashWithHex(String base64EncodedHash)
    {
        try
        {
            return new String(Hex.encode(Base64.getDecoder().decode(Utf8.encode(base64EncodedHash))));
        }
        catch (Exception ex)
        {
            logger.warn("decode hash using base64! " + ex.getMessage(), ex);
        }

        return base64EncodedHash;
    }

    private String extractEncodedPassword(String prefixEncodedPassword)
    {
        int start = prefixEncodedPassword.indexOf(SUFFIX);

        return prefixEncodedPassword.substring(start + 1);
    }

    private String extractId(String prefixEncodedPassword)
    {
        int start = prefixEncodedPassword.indexOf(PREFIX);

        if (start != 0)
        {
            return EMPTY_STRING;
        }

        int end = prefixEncodedPassword.indexOf(SUFFIX, start);

        if (end < 0)
        {
            return EMPTY_STRING;
        }

        return prefixEncodedPassword.substring(start + 1, end);
    }


    /**
     * Getting value whether Base64EncodedPassword is enabled or not
     *
     * @return boolean
     */
    public boolean isBase64EncodedPassword()
    {
        return isBase64EncodedPassword;
    }


    /**
     * Setting value whether Base64EncodedPassword is enabled or not
     *
     * @param base64EncodedPassword
     */
    public void setBase64EncodedPassword(boolean base64EncodedPassword)
    {
        isBase64EncodedPassword = base64EncodedPassword;
    }
}
