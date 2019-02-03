package org.carlspring.strongbox.cron.controller;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.domain.GroovyScriptNamesDto;
import org.carlspring.strongbox.cron.jobs.GroovyCronJob;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationForm;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Defines cron task processing API.
 *
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/crontasks")
@PreAuthorize("hasAuthority('ADMIN')")
public class CronTaskController
        extends BaseController
{

    static final String CRON_CONFIG_FILE_NAME_KEY = "fileName";
    static final String CRON_CONFIG_JOB_CLASS_KEY = "jobClass";
    static final String CRON_CONFIG_SCRIPT_PATH_KEY = "script.path";

    private static final String SUCCESSFUL_SAVE_CONFIGURATION = "The configuration was saved successfully.";
    private static final String FAILED_SAVE_CONFIGURATION = "Could not save the configuration.";

    private static final String SUCCESSFUL_UPDATE_CONFIGURATION = "The configuration was updated successfully.";
    private static final String FAILED_UPDATE_CONFIGURATION = "Could not update the configuration.";

    private static final String SUCCESSFUL_DELETE_CONFIGURATION = "The configuration was deleted successfully.";
    private static final String FAILED_DELETE_CONFIGURATION = "Could not delete the configuration.";

    private static final String SUCCESSFUL_GET_CONFIGURATIONS = "Configurations retrieved successfully.";
    private static final String NOT_FOUND_CONFIGURATIONS = "There are no cron task configs";

    private static final String SUCCESSFUL_GET_CONFIGURATION = "The configuration retrieved successfully.";
    private static final String NOT_FOUND_CONFIGURATION = "Cron task config not found by this uuid!";

    private static final String SUCCESSFUL_UPLOAD_GROOVY_SCRIPT = "The groovy script uploaded successfully.";
    private static final String FAILED_UPLOAD_GROOVY_SCRIPT = "Could not upload the groovy script.";

    private static final String SUCCESSFUL_GET_GROOVY_SCRIPTS = "The groovy scripts named retrieved successfully.";

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;

    @Inject
    private ConversionService conversionService;

    @Inject
    private PropertiesBooter propertiesBooter;


    @ApiOperation(value = "Used to save a new configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_SAVE_CONFIGURATION),
                            @ApiResponse(code = 400, message = FAILED_SAVE_CONFIGURATION) })
    @PutMapping(value = "/",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity createConfiguration(@RequestBody @Validated CronTaskConfigurationForm cronTaskConfigurationForm,
                                              BindingResult bindingResult,
                                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_SAVE_CONFIGURATION, bindingResult);
        }

        try
        {
            CronTaskConfigurationDto cronTaskConfiguration = conversionService.convert(cronTaskConfigurationForm,
                                                                                       CronTaskConfigurationDto.class);
            cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

            return getSuccessfulResponseEntity(SUCCESSFUL_SAVE_CONFIGURATION, acceptHeader);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                                              FAILED_SAVE_CONFIGURATION,
                                              e,
                                              acceptHeader);
        }
    }

    @ApiOperation(value = "Used to update an existing configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_UPDATE_CONFIGURATION),
                            @ApiResponse(code = 400, message = FAILED_UPDATE_CONFIGURATION) })
    @PutMapping(value = "{UUID}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateConfiguration(@PathVariable("UUID") String uuid,
                                              @RequestBody @Validated CronTaskConfigurationForm cronTaskConfigurationForm,
                                              BindingResult bindingResult,
                                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_UPDATE_CONFIGURATION, bindingResult);
        }

        CronTaskConfigurationDto configuration = cronTaskConfigurationService.getTaskConfigurationDto(uuid);
        if (configuration == null)
        {
            return getBadRequestResponseEntity(FAILED_UPDATE_CONFIGURATION, acceptHeader);
        }

        try
        {
            CronTaskConfigurationDto cronTaskConfiguration = conversionService.convert(cronTaskConfigurationForm,
                                                                                       CronTaskConfigurationDto.class);
            cronTaskConfiguration.setUuid(uuid);
            cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

            return getSuccessfulResponseEntity(SUCCESSFUL_SAVE_CONFIGURATION, acceptHeader);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_SAVE_CONFIGURATION, e,
                                              acceptHeader);
        }
    }

    @ApiOperation(value = "Used to delete the configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_DELETE_CONFIGURATION),
                            @ApiResponse(code = 400, message = FAILED_DELETE_CONFIGURATION) })
    @DeleteMapping(value = "{UUID}",
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteConfiguration(@PathVariable("UUID") String uuid,
                                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        final CronTaskConfigurationDto config = cronTaskConfigurationService.getTaskConfigurationDto(uuid);
        if (config == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_CONFIGURATION, acceptHeader);
        }

        try
        {
            cronTaskConfigurationService.deleteConfiguration(config.getUuid());
            if (config.contains(CRON_CONFIG_JOB_CLASS_KEY))
            {
                String className = config.getProperty(CRON_CONFIG_JOB_CLASS_KEY);
                Class<?> c = Class.forName(className);
                Object classInstance = c.getConstructor().newInstance();

                logger.debug("> {}", c.getSuperclass().getCanonicalName());

                if (classInstance instanceof GroovyCronJob)
                {
                    Path path = Paths.get(config.getProperty(CRON_CONFIG_SCRIPT_PATH_KEY));
                    Files.deleteIfExists(path);
                }
            }
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_DELETE_CONFIGURATION, e,
                                              acceptHeader);
        }


        final String message = String.format("Configuration %s removed", uuid);
        logger.debug(message);

        return getSuccessfulResponseEntity(SUCCESSFUL_DELETE_CONFIGURATION, acceptHeader);
    }

    @ApiOperation(value = "Used to get the configuration on given cron task UUID")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GET_CONFIGURATION),
                            @ApiResponse(code = 404, message = NOT_FOUND_CONFIGURATION)})
    @GetMapping(value = "/{UUID}",
                produces = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity getConfiguration(@PathVariable("UUID") String uuid,
                                           @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        CronTaskConfigurationDto config = cronTaskConfigurationService.getTaskConfigurationDto(uuid);
        if (config == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_CONFIGURATION, acceptHeader);
        }

        return ResponseEntity.ok(config);
    }

    @ApiOperation(value = "Used to get list of all the configurations")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GET_CONFIGURATIONS),
                            @ApiResponse(code = 404, message = NOT_FOUND_CONFIGURATIONS) })
    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity getConfigurations(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        CronTasksConfigurationDto config = cronTaskConfigurationService.getTasksConfigurationDto();
        if (config == null || CollectionUtils.isEmpty(config.getCronTaskConfigurations()))
        {
            return getNotFoundResponseEntity(NOT_FOUND_CONFIGURATIONS, acceptHeader);
        }

        return ResponseEntity.ok(config);
    }

    @ApiOperation(value = "Used to upload groovy script for groovy cron task")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_UPLOAD_GROOVY_SCRIPT),
                            @ApiResponse(code = 400, message = FAILED_UPLOAD_GROOVY_SCRIPT) })
    @PutMapping(value = "/cron/groovy/{UUID}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity uploadGroovyScript(@PathVariable("UUID") String uuid,
                                             HttpServletRequest request,
                                             @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {

        final String fileName = request.getHeader(CRON_CONFIG_FILE_NAME_KEY);
        if (!fileName.endsWith(".groovy"))
        {
            return getBadRequestResponseEntity("The uploaded file must be a Groovy one!", acceptHeader);
        }

        CronTaskConfigurationDto cronTaskConfiguration = cronTaskConfigurationService.getTaskConfigurationDto(uuid);
        if (cronTaskConfiguration == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_CONFIGURATION, acceptHeader);
        }

        logger.info(">> CRON UUID: {}", cronTaskConfiguration.getUuid());
        logger.info(">> CRON NAME: {}", cronTaskConfiguration.getName());
        logger.info(">> Properties: {}",  cronTaskConfiguration.getProperties());

        String path = propertiesBooter.getVaultDirectory() + "/etc/conf/cron/groovy";

        cronTaskConfiguration.addProperty(CRON_CONFIG_FILE_NAME_KEY, fileName);
        cronTaskConfiguration.addProperty(CRON_CONFIG_JOB_CLASS_KEY, GroovyCronJob.class.getName());
        cronTaskConfiguration.addProperty(CRON_CONFIG_SCRIPT_PATH_KEY, path + "/" + fileName);

        try
        {
            storeGroovyCronTask(request.getInputStream(), path, fileName);
            cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_UPLOAD_GROOVY_SCRIPT, e,
                                              acceptHeader);
        }

        return getSuccessfulResponseEntity(SUCCESSFUL_UPLOAD_GROOVY_SCRIPT, acceptHeader);
    }

    @ApiOperation(value = "Used to get all groovy scripts names")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_GET_GROOVY_SCRIPTS) })
    @GetMapping(value = "/groovy/names",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getGroovyScriptsName()
    {
        GroovyScriptNamesDto groovyScriptNames = cronJobSchedulerService.getGroovyScriptsName();

        return ResponseEntity.ok(groovyScriptNames);
    }

    private void storeGroovyCronTask(InputStream is,
                                     String dirPath,
                                     String fileName)
            throws IOException
    {
        Path dir = Paths.get(dirPath);

        if (!dir.toFile().exists())
        {
            Files.createDirectories(dir);
        }

        Path file = dir.resolve(fileName);
        Files.copy(is, file);
    }

}
