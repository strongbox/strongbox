package org.carlspring.strongbox.services.impl;

import java.util.HashMap;
import java.util.List;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Service
@Transactional
public class ArtifactTagServiceImpl extends CommonCrudService<ArtifactTagEntry> implements ArtifactTagService
{

    @Override
    @Cacheable(value = CacheName.Artifact.TAGS, key = "#name")
    public synchronized ArtifactTag findOneOrCreate(String name)
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);

        String sQuery = buildQuery(params);

        OSQLSynchQuery<Long> oQuery = new OSQLSynchQuery<>(sQuery);
        oQuery.setLimit(1);

        List<ArtifactTagEntry> resultList = getDelegate().command(oQuery).execute(params);

        return resultList.stream().findFirst().orElseGet(() -> {
            ArtifactTagEntry artifactTagEntry = new ArtifactTagEntry();
            artifactTagEntry.setName(name);
            return getDelegate().detach(save(artifactTagEntry));
        });
    }

    @Override
    public Class<ArtifactTagEntry> getEntityClass()
    {
        return ArtifactTagEntry.class;
    }

}
