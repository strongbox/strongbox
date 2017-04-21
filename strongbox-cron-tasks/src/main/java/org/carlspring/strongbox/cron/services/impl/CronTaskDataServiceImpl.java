package org.carlspring.strongbox.cron.services.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * @author Yougeshwar
 */

@Service
@Transactional
public class CronTaskDataServiceImpl
        extends CommonCrudService<CronTaskConfiguration> implements CronTaskDataService
{

    private static final Logger logger = LoggerFactory.getLogger(CronTaskDataService.class);

    
    
    @Override
    public Class<CronTaskConfiguration> getEntityClass()
    {
        return CronTaskConfiguration.class;
    }



    @Transactional
    public List<CronTaskConfiguration> findByName(String name)
    {
        try
        {
            String sQuery = String.format("select * from %s where name=:name", getEntityClass().getSimpleName());
            OSQLSynchQuery<CronTaskConfiguration> oQuery = new OSQLSynchQuery<CronTaskConfiguration>(sQuery);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("name", name);
            return getDelegate().command(oQuery).execute(params);
        }
        catch (Exception e)
        {
            logger.warn("Internal spring-data-orientdb exception: " + e.getMessage(), e);
            return new LinkedList<>();
        }
    }
    
    

}
