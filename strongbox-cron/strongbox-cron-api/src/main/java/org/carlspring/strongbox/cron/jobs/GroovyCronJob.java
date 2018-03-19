package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

/**
 * @author carlspring
 * @author Yougeshwar
 */
public class GroovyCronJob
        extends AbstractCronJob
{

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        try
        {
            Class scriptClass = new GroovyClassLoader().parseClass(
                    new GroovyCodeSource(Paths.get(getScriptPath()).toUri()));
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
