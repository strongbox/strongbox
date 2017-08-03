package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.data.service.CommonCrudService;

import java.util.*;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yougeshwar
 */

@Service
@Transactional
public class CronTaskDataServiceImpl
        extends CommonCrudService<CronTaskConfiguration>
        implements CronTaskDataService
{

    private static final Logger logger = LoggerFactory.getLogger(CronTaskDataService.class);


    @Transactional
    public List<CronTaskConfiguration> findByName(String name)
    {
        try
        {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("name", name);

            String sQuery = buildQuery(params);
            OSQLSynchQuery<CronTaskConfiguration> oQuery = new OSQLSynchQuery<>(sQuery);

            return getDelegate().command(oQuery).execute(params);
        }
        catch (Exception e)
        {
            logger.warn(e.getMessage(), e);

            return new LinkedList<>();
        }
    }

    @Override
    public Class<CronTaskConfiguration> getEntityClass()
    {
        return CronTaskConfiguration.class;
    }

}
