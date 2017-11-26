package org.carlspring.strongbox.providers.io;

import java.util.Arrays;

public enum RepositoryFileAttributeType
{

    COORDINATES("coordinates"),

    METADATA("metadata"),

    CHECKSUM("checksum"),

    TRASH("trash"),

    TEMP("temp"),

    INDEX("index"),

    ARTIFACT("artifact");

    private String name;

    private RepositoryFileAttributeType(String name)
    {
        this.name = name;
    }

    protected String getName()
    {
        return name;
    }

    public static RepositoryFileAttributeType of(String s)
    {
        return Arrays.stream(values())
                     .filter(e -> e.getName().equals(s))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(s));
    }

}
