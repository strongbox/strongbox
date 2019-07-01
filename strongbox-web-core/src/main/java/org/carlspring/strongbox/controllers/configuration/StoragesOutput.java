package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.Views;

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
    private List<StorageData> storages;

    public StoragesOutput()
    {
    }

    public StoragesOutput(final List<StorageData> storages)
    {
        this.storages = storages;
    }

    public List<StorageData> getStorages()
    {
        return storages;
    }

    public void setStorages(final List<StorageData> storages)
    {
        this.storages = storages;
    }
}
