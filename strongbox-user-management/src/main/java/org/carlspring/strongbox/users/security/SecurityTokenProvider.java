package org.carlspring.strongbox.users.security;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Used to get and verify security tokens. <br>
 * This implementation based on JSON Web Token (JWT) which is RFC 7519 standard. <br>
 * 
 * 
 * @author Sergey Bespalov
 */
@Component
public class SecurityTokenProvider
{
    private static final String MESSAGE_INVALID_JWT = "Invalid JWT: value-[%s]";
    /**
     * Secret key which is used to encode and verify tokens.<br>
     * All previous tokens will be invalid, if it changed.
     */
    private Key key;

    /**
     * Creates {@link Key} instance using Secret string from application configuration.
     * 
     * @param secret
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    @Autowired
    public void init(@Value("${strongbox.security.jwtSecret:secret}") String secret)
        throws UnsupportedEncodingException
    {
        key = new HmacKey(secret.getBytes("UTF-8"));
    }

    /**
     * Generates an encrypted token.
     * 
     * @param subject
     *            a Subject which is used as token base.
     * @param claimMap
     *            an additional Claims which will also present in token.
     * @param expire
     *            token exparation date.
     * @return encrypted token string.
     * @throws JoseException
     */
    public String getToken(String subject,
                           Map<String, String> claimMap,
                           Date expire)
        throws JoseException
    {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("Strongbox");
        claims.setGeneratedJwtId();
        claims.setSubject(subject);
        claimMap.entrySet().stream().forEach((e) -> {
            claims.setClaim(e.getKey(), e.getValue());
        });

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setDoKeyValidation(false);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);

        return jws.getCompactSerialization();
    }

    /**
     * @param token
     * @param targetSubject
     * @param claimMap
     */
    public void verifyToken(String token,
                            String targetSubject,
                            Map<String, String> claimMap)
    {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireSubject()
                                                          .setVerificationKey(key)
                                                          .setRelaxVerificationKeyValidation()
                                                          .build();

        JwtClaims jwtClaims;
        String subject;
        try
        {
            jwtClaims = jwtConsumer.processToClaims(token);
            subject = jwtClaims.getSubject();
        }
        catch (InvalidJwtException | MalformedClaimException e)
        {
            throw new SecurityTokenException(String.format(MESSAGE_INVALID_JWT, token), e);
        }

        if (!targetSubject.equals(subject))
        {
            throw new SecurityTokenException(String.format(MESSAGE_INVALID_JWT, token));
        }

        boolean claimMatch = claimMap.entrySet().stream().allMatch((e) -> {
            return e.getValue().equals(jwtClaims.getClaimValue(e.getKey()));
        });

        if (!claimMatch)
        {
            throw new SecurityTokenException(String.format(MESSAGE_INVALID_JWT, token));
        }
    }

}
