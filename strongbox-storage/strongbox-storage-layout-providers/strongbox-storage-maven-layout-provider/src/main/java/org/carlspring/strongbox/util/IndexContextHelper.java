package org.carlspring.strongbox.util;

import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;

/**
 * @author carlspring
 */
public class IndexContextHelper
{

    public static String getContextId(String storageId,
                                      String repositoryId,
                                      IndexTypeEnum indexType)
    {
        return getContextId(storageId, repositoryId, indexType.getType());
    }

    public static String getContextId(String storageId,
                                      String repositoryId,
                                      String indexType)
    {
        return storageId + ":" + repositoryId + ":" + indexType;
    }

}
