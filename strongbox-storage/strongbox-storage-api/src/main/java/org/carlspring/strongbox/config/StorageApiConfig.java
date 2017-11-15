package org.carlspring.strongbox.config;

import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.LinkedHashSet;
import java.util.List;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.xml"
               })
public class StorageApiConfig
{

    @Inject
    private List<VersionValidator> versionValidators;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private OEntityManager oEntityManager;
    
    @Inject
    private TransactionTemplate transactionTemplate;

    
    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) ->
        {
            doInit();
            return null;
        });
    }

    private void doInit()
    {
        // register all domain entities
        oEntityManager.registerEntityClass(ArtifactEntry.class);
        oEntityManager.registerEntityClass(RemoteArtifactEntry.class);

        OClass artifactEntryClass = ((OObjectDatabaseTx) entityManager.getDelegate()).getMetadata()
                                                                                     .getSchema()
                                                                                     .getClass(ArtifactEntry.class);
        if (artifactEntryClass.getIndexes()
                              .stream()
                              .noneMatch(oIndex -> oIndex.getName().equals("idx_artifact")))
        {
            artifactEntryClass.createIndex("idx_artifact", OClass.INDEX_TYPE.UNIQUE, "storageId", "repositoryId",
                                           "artifactPath");
        }

        OClass artifactCoordinatesClass = ((OObjectDatabaseTx) entityManager.getDelegate()).getMetadata()
                                                                                           .getSchema()
                                                                                           .getClass(AbstractArtifactCoordinates.class);
        if (artifactCoordinatesClass.getIndexes()
                                    .stream()
                                    .noneMatch(oIndex -> oIndex.getName().equals("idx_artifact_coordinates")))
        {
            artifactCoordinatesClass.createIndex("idx_artifact_coordinates", OClass.INDEX_TYPE.UNIQUE, "path");
        }
        
    }

    @Bean(name = "checksumCacheManager")
    ChecksumCacheManager checksumCacheManager()
    {
        ChecksumCacheManager checksumCacheManager = new ChecksumCacheManager();
        checksumCacheManager.setCachedChecksumExpiredCheckInterval(300000);
        checksumCacheManager.setCachedChecksumLifetime(60000);

        return checksumCacheManager;
    }

    @Bean(name = "versionValidators")
    LinkedHashSet<VersionValidator> versionValidators()
    {
        return new LinkedHashSet<>(versionValidators);
    }

    @Bean(name = "resourcesBooter")
    ResourcesBooter getResourcesBooter()
    {
        return new ResourcesBooter();
    }

    @Bean(name = "storageBooter")
    StorageBooter getStorageBooter()
    {
        return new StorageBooter();
    }

}
