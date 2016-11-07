package org.carlspring.strongbox.cron.api.jobs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import groovy.lang.GroovyClassLoader;
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
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        try
        {
            Class scriptClass = new GroovyClassLoader().parseClass(new File(getScriptPath()));
            Object scriptInstance = scriptClass.newInstance();
            scriptClass.getDeclaredMethod("execute", new Class[]{}).invoke(scriptInstance, new Object[]{});
        }
        catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e)
        {
            e.printStackTrace();
            logger.error("IOException: ", e);
        }

    }

    public String getScriptPath()
    {
        return (String) getConfiguration().getProperties().get("script.path");
    }

}
