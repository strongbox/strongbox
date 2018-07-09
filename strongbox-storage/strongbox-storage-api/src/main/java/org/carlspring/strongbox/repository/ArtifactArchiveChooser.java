package org.carlspring.strongbox.repository;

import java.io.File;

/**
 * @author Przemyslaw Fusik
 */
@FunctionalInterface
public interface ArtifactArchiveChooser
{
    boolean accept(String filename);
}
