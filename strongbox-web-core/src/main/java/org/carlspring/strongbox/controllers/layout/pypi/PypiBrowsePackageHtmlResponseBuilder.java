package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;

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

    private final Logger logger = LoggerFactory.getLogger(PypiBrowsePackageHtmlResponseBuilder.class);

    public String getHtmlResponse(List<Path> filePaths,
                                  String packageName,
                                  Repository repository)
    {
        String htmlResponse = "<html>\n"+
                              "        <head>\n"+
                              "            <title>Links for " + packageName + "</title>\n"+
                              "        </head>\n"+
                              "        <body>\n"+
                              "            <h1>Links for " + packageName + "</h1>\n"+
                              "                   " + getPackageLinks(filePaths, repository)+
                              "        </body>\n"+
                              "    </html>";

        return htmlResponse;
    }

    private String getPackageLinks(List<Path> filePaths,
                                   Repository repository)
    {

        String packageLinks = "";

        for (Path path : filePaths)
        {
            PypiArtifactCoordinates artifactCoordinates = null;
            try
            {
                artifactCoordinates = (PypiArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath) path);
            }
            catch (Exception e)
            {
                logger.error("Failed to read python package path [{}]", path, e);
                continue;
            }
            packageLinks += "<a href=\"" + "/storages/" + repository.getStorage().getId() + "/" + repository.getId() +
                            "/packages/" + artifactCoordinates.buildWheelPackageFileName() + "\">" +
                            artifactCoordinates.buildWheelPackageFileName() + "</a><br>\n";
        }

        return packageLinks;
    }

}
