package org.carlspring.strongbox.cron.controller;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.domain.GroovyScriptNamesDto;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.jobs.GroovyCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.collections.CollectionUtils;
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
@RequestMapping("/api/configuration/crontasks")
@PreAuthorize("hasAuthority('ADMIN')")
public class CronTaskController
        extends BaseController
{
    @Inject
    CronTaskConfigurationService cronTaskConfigurationService;
    
    @Inject
    CronJobSchedulerService cronJobSchedulerService;

    @ApiOperation(value = "Used to save the configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was saved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron",
                    method = RequestMethod.PUT,
                    consumes = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML }
    )
    public ResponseEntity saveConfiguration(@RequestBody CronTaskConfigurationDto cronTaskConfiguration)
    {
        try
        {
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

    @ApiOperation(value = "Used to delete the configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was deleted successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron", method = RequestMethod.DELETE)
    public ResponseEntity deleteConfiguration(@RequestParam("name") String name)
    {
        final List<Exception> errors = new LinkedList<>();
        final CronTaskConfigurationDto config = cronTaskConfigurationService.getTaskConfigurationDto(name);
        if (config != null)
        {
            try
            {
                cronTaskConfigurationService.deleteConfiguration(config.getName());
                if (config.contains("jobClass"))
                {
                    Class c = Class.forName(config.getProperty("jobClass"));
                    Object classInstance = c.getConstructor().newInstance();

                    logger.debug("> " + c.getSuperclass().getCanonicalName());

                    if (classInstance instanceof GroovyCronJob)
                    {
                        Path path = Paths.get(config.getProperty("script.path"));
                        Files.deleteIfExists(path);
                    }
                }
            }
            catch (Exception e)
            {
                errors.add(e);
            }
        }

        if (!errors.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.get(0).getMessage());
        }

        return ResponseEntity.ok().body("Configuration " + name + " removed");
    }

    @ApiOperation(value = "Used to get the configuration on given cron task name")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration retrieved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/cron",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML })
    public ResponseEntity getConfiguration(@RequestParam("name") String name)
    {
        CronTaskConfigurationDto config = cronTaskConfigurationService.getTaskConfigurationDto(name);
        if (config == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Cron task config not found by this name!");
        }

        try
        {
            return ResponseEntity.ok(config);
        }
        catch (Exception e)
        {
            logger.error("Unable to serialize config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ApiOperation(value = "Used to get list of all the configurations")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The all configurations retrieved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML })
    public ResponseEntity getConfigurations()
    {
        CronTasksConfigurationDto config = cronTaskConfigurationService.getTasksConfigurationDto();
        if (config == null || CollectionUtils.isEmpty(config.getCronTaskConfigurations()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("There are no cron task configs");
        }

        return ResponseEntity.ok(config);
    }

    @ApiOperation(value = "Used to upload groovy script for groovy cron task")
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

        CronTaskConfigurationDto cronTaskConfiguration = cronTaskConfigurationService.getTaskConfigurationDto(cronName);
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

    @ApiOperation(value = "Used to get all groovy scripts names")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The groovy scripts named retrieved successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @RequestMapping(value = "/groovy/names",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON,
                                 MediaType.APPLICATION_XML })
    public ResponseEntity getGroovyScriptsName()
    {
        GroovyScriptNamesDto groovyScriptNames = cronJobSchedulerService.getGroovyScriptsName();

        return ResponseEntity.ok(groovyScriptNames);
    }

    private void storeGroovyCronTask(InputStream is,
                                     String dirPath,
                                     String fileName)
            throws CronTaskException, IOException
    {
        Path dir = Paths.get(dirPath);

        if (!Files.exists(dir))
        {
            Files.createDirectories(dir);
        }

        Path file = dir.resolve(fileName);
        Files.copy(is, file);
    }

}
