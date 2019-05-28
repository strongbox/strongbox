package org.carlspring.strongbox.controllers.ssl;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.controllers.users.UserController;
import org.carlspring.strongbox.forms.ssl.HostForm;
import org.carlspring.strongbox.security.certificates.AbstractKeyStoreManager;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Przemyslaw Fusik
 */
abstract class AbstractKeyStoreManagementController
        extends BaseController
{

    abstract AbstractKeyStoreManager getKeyStoreManager();

    @GetMapping(value = "/size", produces = { MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity size(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        int size;
        try
        {
            size = getKeyStoreManager().size();
        }
        catch (Exception ex)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "TODO", ex, accept);
        }
        return ResponseEntity.ok(size);
    }

    @GetMapping(value = "/aliases", produces = { MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity listAliases(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        Set<String> body;
        try
        {
            body = getKeyStoreManager().listCertificates().keySet();
        }
        catch (Exception ex)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "TODO", ex, accept);
        }
        return ResponseEntity.ok(body);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity add(@RequestBody @Validated HostForm hostForm,
                       BindingResult bindingResult,
                       @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(UserController.FAILED_UPDATE_USER, bindingResult);
        }
        try
        {
            getKeyStoreManager().addCertificates(hostForm.getName(), hostForm.getPortOrDefault(443));
            return ResponseEntity.ok().build();
        }
        catch (Exception ex)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "TODO", ex, accept);
        }
    }

    @PutMapping(value = "/proxied", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity addWithProxy(@RequestBody @Validated HostForm hostForm,
                                BindingResult bindingResult,
                                @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(UserController.FAILED_UPDATE_USER, bindingResult);
        }
        try
        {
            getKeyStoreManager().addCertificates(hostForm.getName(), hostForm.getPortOrDefault(443));
            return ResponseEntity.ok().build();
        }
        catch (Exception ex)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "TODO", ex, accept);
        }
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity remove(@RequestBody @Validated HostForm hostForm,
                          BindingResult bindingResult,
                          @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(UserController.FAILED_UPDATE_USER, bindingResult);
        }
        try
        {
            getKeyStoreManager().removeCertificates(hostForm.getName(), hostForm.getPortOrDefault(443));
            return ResponseEntity.ok().build();
        }
        catch (Exception ex)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "TODO", ex, accept);
        }
    }
}
