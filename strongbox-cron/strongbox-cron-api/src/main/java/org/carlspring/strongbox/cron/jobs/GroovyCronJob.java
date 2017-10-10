package org.carlspring.strongbox.cron.jobs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import groovy.lang.GroovyClassLoader;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 * @author Yougeshwar
 */
public class GroovyCronJob
        extends AbstractCronJob
{

    private static final Logger logger = LoggerFactory.getLogger(GroovyCronJob.class);


    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        try
        {
            Class scriptClass = new GroovyClassLoader().parseClass(new File(getScriptPath()));
            Object scriptInstance = scriptClass.newInstance();
            //noinspection unchecked
            scriptClass.getDeclaredMethod("execute", new Class[]{}).invoke(scriptInstance);
        }
        catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
        {
            logger.error("IOException: ", e);
        }
    }

    public String getScriptPath()
    {
        return getConfiguration().getProperties().get("script.path");
    }

}
