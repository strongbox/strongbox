package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
    @SuppressFBWarnings(value = "DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
    public void executeTask(CronTaskConfigurationDto config)
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
        return configuration.getProperties().get("script.path");
    }

}
