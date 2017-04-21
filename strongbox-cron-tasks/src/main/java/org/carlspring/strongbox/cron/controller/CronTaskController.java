package org.carlspring.strongbox.cron.controller;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.cron.api.jobs.GroovyCronJob;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.quartz.GroovyScriptNames;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Defines cron task processing API.
 *
 * @author Alex Oreshkevich
 */
@Controller
@RequestMapping("/configuration/crontasks")
@PreAuthorize("hasAuthority('ADMIN')")
public class CronTaskController
        extends BaseController
{

    private final GenericParser<CronTaskConfiguration> configParser = new GenericParser<>(CronTaskConfiguration.class);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    OObjectDatabaseTx databaseTx;


    @ApiOperation(value = "Used to save the configuration", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was saved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron",
                    method = RequestMethod.PUT,
                    consumes = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML }
    )
    public ResponseEntity saveConfiguration(@RequestBody CronTaskConfiguration cronTaskConfiguration)
    {
        try
        {
            logger.debug("Cron task configuration: " + objectMapper.writeValueAsString(cronTaskConfiguration));
            logger.debug("Save Cron Task config call");

            cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

            return ResponseEntity.ok().build();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to delete the configuration", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was deleted successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron", method = RequestMethod.DELETE)
    public ResponseEntity deleteConfiguration(@RequestParam("name") String name)
    {
        final List<Exception> errors = new LinkedList<>();
        cronTaskConfigurationService.getConfiguration(name)
                                    .forEach(config -> {
                                        try
                                        {
                                            cronTaskConfigurationService.deleteConfiguration(config);
                                            if (config.contain("jobClass"))
                                            {
                                                Class c = Class.forName(config.getProperty("jobClass"));
                                                Object classInstance = c.newInstance();

                                                logger.debug("> " + c.getSuperclass().getCanonicalName());

                                                if (classInstance instanceof GroovyCronJob)
                                                {
                                                    File file = new File(config.getProperty("script.path"));
                                                    if (file.exists())
                                                    {
                                                        //noinspection ResultOfMethodCallIgnored
                                                        file.delete();
                                                    }
                                                }
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            errors.add(e);
                                        }
                                    });

        if (!errors.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.get(0).getMessage());
        }

        return ResponseEntity.ok().body("Configuration " + name + " removed");
    }

    @ApiOperation(value = "Used to get the configuration on given cron task name", position = 2)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration retrieved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML })
    public ResponseEntity getConfiguration(@RequestParam("name") String name)
    {
        CronTaskConfiguration config = cronTaskConfigurationService.findOne(name);
        if (config == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Cron task config not found by this name!");
        }

        try
        {
            return ResponseEntity.ok(configParser.serialize(config));
        }
        catch (Exception e)
        {
            logger.error("Unable to serialize config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to get list of all the configurations", position = 3)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The all configurations retrieved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML })
    public ResponseEntity getConfigurations()
    {
        List<CronTaskConfiguration> configList = cronTaskConfigurationService.getConfigurations();
        if (configList == null || configList.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("There are no cron task configs");
        }

        return ResponseEntity.ok(configList);
    }

    @ApiOperation(value = "Used to upload groovy script for groovy cron task", position = 4)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The groovy script uploaded successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron/groovy", method = RequestMethod.PUT)
    public ResponseEntity uploadGroovyScript(@RequestParam("cronName") String cronName,
                                             HttpServletRequest request)
    {

        String fileName = request.getHeader("fileName");
        if (!fileName.endsWith(".groovy"))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("The uploaded file must be a Groovy one!");
        }

        CronTaskConfiguration cronTaskConfiguration = cronTaskConfigurationService.findOne(cronName);
        if (cronTaskConfiguration == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Configuration not found by this name!");
        }

        logger.info(">> CRON NAME: " + cronTaskConfiguration.getName());
        logger.info(">> Properties: " + cronTaskConfiguration.getProperties());

        String path = ConfigurationResourceResolver.getVaultDirectory() + "/etc/conf/cron/groovy";

        cronTaskConfiguration.addProperty("fileName", fileName);
        cronTaskConfiguration.addProperty("jobClass", GroovyCronJob.class.getName());
        cronTaskConfiguration.addProperty("script.path", path + "/" + fileName);

        try
        {
            storeGroovyCronTask(request.getInputStream(), path, fileName);
            cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Used to get all groovy scripts names", position = 5)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The groovy scripts named retrieved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/groovy/names",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML })
    public ResponseEntity getGroovyScriptsName()
    {
        GroovyScriptNames groovyScriptNames = cronTaskConfigurationService.getGroovyScriptsName();

        return ResponseEntity.ok(groovyScriptNames);
    }

    private void storeGroovyCronTask(InputStream is,
                                     String dirPath,
                                     String fileName)
            throws CronTaskException
    {
        File dir = new File(dirPath);

        if (!dir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        File file = new File(dirPath + "/" + fileName);

        try (OutputStream out = new FileOutputStream(file))
        {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1)
            {
                out.write(bytes, 0, read);
            }
            out.flush();
        }
        catch (IOException e)
        {
            throw new CronTaskException(e);
        }
    }

}
