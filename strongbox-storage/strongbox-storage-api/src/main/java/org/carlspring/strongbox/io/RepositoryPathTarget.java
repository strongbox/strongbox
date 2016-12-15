package org.carlspring.strongbox.io;

import java.nio.file.Path;

public interface RepositoryPathTarget
{

    Path getRepositoryRoot();

    Path getPath();

}
