package org.carlspring.strongbox.util.encryption;

/**
 * @author mtodorov
 */
public enum EncryptionAlgorithms
{

    MD5("MD5"),

    SHA1("SHA1"),

    SHA256("SHA256"),

    PLAIN("plain");

    private String algorithm;


    EncryptionAlgorithms(String algorithm)
    {
        this.algorithm = algorithm;
    }

    @Override
    public String toString()
    {
        return algorithm;
    }

}
