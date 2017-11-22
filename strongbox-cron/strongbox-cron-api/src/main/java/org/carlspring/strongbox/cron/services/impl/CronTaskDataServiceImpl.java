package org.carlspring.strongbox.cron.services.impl;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;
import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;

import java.util.*;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    public List<CronTaskConfiguration> findMatching(CronTaskConfigurationSearchCriteria searchCriteria,
                                                    PagingCriteria pagingCriteria)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT FROM ").append(getEntityClass().getSimpleName());
        Map<String, Object> parameterMap = Collections.emptyMap();

        if (!searchCriteria.isEmpty())
        {
            StringBuilder criteriaQueryClasuse = new StringBuilder();
            sb.append(" WHERE ");
            parameterMap = new HashMap<>();

            if (!CollectionUtils.isEmpty(searchCriteria.getProperties()))
            {
                for (Map.Entry<String, String> entry : searchCriteria.getProperties().entrySet())
                {
                    final String property = entry.getKey();
                    criteriaQueryClasuse.append(" properties[" + property + "] = :" + property + " ");
                    parameterMap.put(property, entry.getValue());
                    criteriaQueryClasuse.append(" AND ");
                }
                criteriaQueryClasuse.setLength(criteriaQueryClasuse.length() - 5);
            }

            sb.append(criteriaQueryClasuse);
        }

        appendPagingCriteria(sb, pagingCriteria);

        logger.debug("Executing SQL query> " + sb.toString());

        OSQLSynchQuery<CronTaskConfiguration> oQuery = new OSQLSynchQuery<>(sb.toString());

        return getDelegate().command(oQuery).execute(parameterMap);
    }

    @Override
    public Class<CronTaskConfiguration> getEntityClass()
    {
        return CronTaskConfiguration.class;
    }

}
