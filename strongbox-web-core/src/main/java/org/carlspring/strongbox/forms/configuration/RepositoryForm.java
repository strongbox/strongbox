package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.validation.configuration.RepositoryPolicyValue;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryForm
{

    @NotEmpty
    private String id;

    private String basedir;

    @NotEmpty
    @RepositoryPolicyValue(message = "policy value must be one of {0}")
    private String policy;

    @NotEmpty
    @RepositoryImplementationValue
    private String implementation;

    @NotEmpty
    @ReposityrLayoutValue
    private String layout;

    @NotEmpty
    @RepositoryTypeValue
    private String type;

    private boolean secured;

    @NotEmpty
    @RepositoryStatusValue
    private String status;

    private long artifactMaxSize;

    private boolean trashEnabled;

    private boolean allowsForceDeletion;

    private boolean allowsDeployment = true;

    private boolean allowsRedeployment = true;

    private boolean allowsDelete = true;

    private boolean allowsDirectoryBrowsing = true;

    private boolean checksumHeadersEnabled;

    @Valid
    private ProxyConfigurationForm proxyConfiguration;

    @Valid
    private RemoteRepositoryForm remoteRepository;

    @PositiveOrZero
    private Integer httpConnectionPool;

    private Set<String> groupRepositories;

    private Set<String> artifactCoordinateValidators;

}
