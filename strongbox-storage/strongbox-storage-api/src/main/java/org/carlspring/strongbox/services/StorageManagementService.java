package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.StorageDto;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface StorageManagementService
{

    void saveStorage(StorageDto storage)
            throws IOException;

    void removeStorage(String storageId)
            throws IOException;

}
