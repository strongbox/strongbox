package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.api.Describable;

import java.util.stream.Stream;

public enum MetadataStrategyEnum implements Describable
{
    CHECKSUM("checksum"),
    REFRESH("refresh");

    private String strategy;

    MetadataStrategyEnum(String strategy)
    {
        this.strategy = strategy;
    }

    @Override
    public String describe()
    {
        return strategy;
    }

    public static MetadataStrategyEnum ofStrategy(String strategy)
    {
        return Stream.of(values())
                     .filter(e -> e.strategy.equals(strategy))
                     .findFirst()
                     .orElse(null);
    }
}
