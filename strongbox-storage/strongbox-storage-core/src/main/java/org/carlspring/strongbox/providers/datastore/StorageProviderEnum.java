package org.carlspring.strongbox.providers.datastore;

import org.carlspring.strongbox.api.Describable;

/**
 * @author Przemyslaw Fusik
 */
public enum StorageProviderEnum
        implements Describable
{

    FILESYSTEM("file-system");

    private String description;

    StorageProviderEnum(String description)
    {
        this.description = description;
    }

    @Override
    public String describe()
    {
        return description;
    }
}
