package org.carlspring.strongbox.controllers.layout.pypi;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.carlspring.strongbox.testing.repository.PypiTestRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author ankit.tomar
 */
@IntegrationTest
public class PypiBrowsePackageHtmlResponseBuilderTest
{

    private static final String REPOSITORY_RELEASES = "pypi-releases-test";

    private static final String STORAGE_PYPI = "storage-pypi-test";

    @Inject
    private PypiBrowsePackageHtmlResponseBuilder htmlResponseBuilder;

    @Test
    public void testNoPackageFound()
        throws IOException
    {
        String expectedHtmlResponse = "<html>\n" +
                                      "        <head>\n" +
                                      "            <title>Not Found</title>\n" +
                                      "        </head>\n" +
                                      "        <body>\n" +
                                      "            <h1>Not Found</h1>\n" +
                                      "        </body>\n" +
                                      "</html>";

        String htmlResponse = htmlResponseBuilder.getHtmlResponse(new ArrayList<>());

        assertThat(htmlResponse).isNotBlank().isEqualTo(expectedHtmlResponse);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testOnlyOnePackageFound(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES,
                                                            storageId = STORAGE_PYPI) 
                                        Repository repository,
                                        @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                          storageId = STORAGE_PYPI,
                                                          id = "hello_world",
                                                          versions = "1.0.0") 
                                        Path packagePath)
        throws IOException
    {

        PypiArtifactCoordinates artifactCoordinates = (PypiArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath) packagePath.normalize());

        String links = "<a href=\"" + "/storages/" + repository.getStorage().getId() + "/" + repository.getId() +
                       "/packages/" + artifactCoordinates.buildWheelPackageFileName() + "\">" +
                       artifactCoordinates.buildWheelPackageFileName() + "</a><br>\n";

        String expectedHtmlResponse = "<html>\n" +
                                      "        <head>\n" +
                                      "            <title>Links for " + artifactCoordinates.getId() + "</title>\n" +
                                      "        </head>\n" +
                                      "        <body>\n" +
                                      "            <h1>Links for " + artifactCoordinates.getId() + "</h1>\n" +
                                      "                   " + links +
                                      "        </body>\n" +
                                      "</html>";

        List<Path> paths = new ArrayList<>();
        paths.add(packagePath.normalize());
        String htmlResponse = htmlResponseBuilder.getHtmlResponse(paths);

        assertThat(htmlResponse).isNotBlank().isEqualTo(expectedHtmlResponse);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testMultiplePackagesFound(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES,
                                                              storageId = STORAGE_PYPI)
                                          Repository repository,
                                          @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                            storageId = STORAGE_PYPI,
                                                            id = "hello_world",
                                                            versions = { "1.0",
                                                                         "2.0",
                                                                         "3.0",
                                                                         "4.0",
                                                                         "5.0"}) 
                                          List<Path> packagePaths)
        throws IOException
    {

        List<Path> paths = new ArrayList<>();
        String links = "";
        String packageName = "";
        for (Path path : packagePaths)
        {

            paths.add(path.normalize());
            PypiArtifactCoordinates artifactCoordinates = (PypiArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath) path.normalize());
            packageName = artifactCoordinates.getId();

            links += "<a href=\"" + "/storages/" + repository.getStorage().getId() + "/" + repository.getId() +
                     "/packages/" + artifactCoordinates.buildWheelPackageFileName() + "\">" +
                     artifactCoordinates.buildWheelPackageFileName() + "</a><br>\n";
        }

        String expectedHtmlResponse = "<html>\n" +
                                      "        <head>\n" +
                                      "            <title>Links for " + packageName + "</title>\n" +
                                      "        </head>\n" +
                                      "        <body>\n" +
                                      "            <h1>Links for " + packageName + "</h1>\n" +
                                      "                   " + links +
                                      "        </body>\n" +
                                      "</html>";

        String htmlResponse = htmlResponseBuilder.getHtmlResponse(paths);

        assertThat(htmlResponse).isNotBlank().isEqualTo(expectedHtmlResponse);
    }
}
