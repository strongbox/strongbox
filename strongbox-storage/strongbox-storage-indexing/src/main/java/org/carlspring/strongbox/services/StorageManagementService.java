package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.Storage;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface StorageManagementService
{

    void createStorage(Storage storage)
            throws IOException;

    void removeStorage(String storageId)
            throws IOException;

}
