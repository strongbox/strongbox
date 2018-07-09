package org.carlspring.strongbox.repository;

import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
public enum DefaultArchiveListingFunctionsRegistry
        implements Supplier<Set<ArchiveListingFunction>>
{

    INSTANCE;

    private static final Set<ArchiveListingFunction> FUNCTIONS = ImmutableSet.of(Bzip2ArchiveListingFunction.INSTANCE,
                                                                                 TarArchiveListingFunction.INSTANCE,
                                                                                 TarGzArchiveListingFunction.INSTANCE,
                                                                                 ZipArchiveListingFunction.INSTANCE);


    public Set<ArchiveListingFunction> get()
    {
        return FUNCTIONS;
    }
}
