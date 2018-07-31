package org.carlspring.strongbox.controllers.configuration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * @author Przemyslaw Fusik
 */
@JsonRootName("storages")
public class StoragesOutput
{

    @JsonView(Views.ShortStorage.class)
    private List<StorageOutput> storages;

    public StoragesOutput()
    {
    }

    public StoragesOutput(final List<StorageOutput> storages)
    {
        this.storages = storages;
    }

    public List<StorageOutput> getStorages()
    {
        return storages;
    }

    public void setStorages(final List<StorageOutput> storages)
    {
        this.storages = storages;
    }
}
