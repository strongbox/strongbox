package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;

import javax.inject.Inject;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for {@link ArtifactEntry} entities.
 *
 * @author Sergey Bespalov
 */
@Service
@Transactional
abstract class AbstractArtifactEntryService
        extends CommonCrudService<ArtifactEntry>
        implements ArtifactEntryService
{

    @Inject
    private ConfigurationManager configurationManager;


    @Override
    public String constructArtifactURL(String storageId,
                                       String repositoryId,
                                       ArtifactCoordinates artifactCoordinates)
    {
        String baseUrl = configurationManager.getConfiguration().getBaseUrl();

        String url = baseUrl + (!baseUrl.endsWith("/") ? "/" : "") +
                     storageId + "/" +
                     repositoryId + "/" +
                     artifactCoordinates.toPath();

        return url;
    }

    @Override
    public String getURLForArtifact(String storageId,
                                    String repositoryId,
                                    String path)
    {
        String baseUrl = configurationManager.getConfiguration().getBaseUrl();
        baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        return baseUrl + "storages/" + storageId + "/" + repositoryId + "/" + path;
    }



}
