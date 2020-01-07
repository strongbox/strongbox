package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Helper class for {@link PypiArtifactController}
 * 
 * @author ankit.tomar
 */
@Component
public class PypiBrowsePackageHtmlResponseBuilder
{

    public String getHtmlResponse(List<Path> filePaths,
                                  String packageName)
        throws IOException
    {
        String htmlResponse = "<html>\n" +
                              "        <head>\n" +
                              "            <title>Links for " + packageName + "</title>\n" +
                              "        </head>\n" +
                              "        <body>\n" +
                              "            <h1>Links for " + packageName + "</h1>\n" +
                              "                   " + getPackageLinks(filePaths) +
                              "        </body>\n" +
                              "    </html>";

        return htmlResponse;
    }

    private String getPackageLinks(List<Path> filePaths)
        throws IOException
    {

        String packageLinks = "";

        for (Path path : filePaths)
        {
            RepositoryPath repositoryPath = (RepositoryPath) path;
            PypiArtifactCoordinates artifactCoordinates = (PypiArtifactCoordinates) RepositoryFiles.readCoordinates(repositoryPath);

            Repository repository = repositoryPath.getRepository();
            packageLinks += "<a href=\"" + "/storages/" + repository.getStorage().getId() + "/" + repository.getId() +
                            "/packages/" + artifactCoordinates.buildWheelPackageFileName() + "\">" +
                            artifactCoordinates.buildWheelPackageFileName() + "</a><br>\n";
        }

        return packageLinks;
    }

}
