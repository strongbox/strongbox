package org.carlspring.strongbox.storage.indexing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.util.zip.ZipFacade;
import org.apache.maven.index.util.zip.ZipHandle;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Przemyslaw Fusik
 */
public class SafeArtifactContext
        extends ArtifactContext
{

    public SafeArtifactContext(ArtifactContext ac)
    {
        super(ac.getPom(), ac.getArtifact(), ac.getMetadata(), ac.getArtifactInfo(), ac.getGav());
    }

    @Override
    public Model getPomModel()
    {
        // First check for local pom file
        if (getPom() != null && getPom().isFile())
        {
            try
            {
                // here is the difference - close streams, guys
                try (InputStream is = new FileInputStream(getPom()))
                {
                    return new MavenXpp3Reader().read(is, false);
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (XmlPullParserException e)
            {
                e.printStackTrace();
            }
        }
        // Otherwise, check for pom contained in maven generated artifact
        else if (getArtifact() != null && getArtifact().isFile())
        {
            ZipHandle handle = null;

            try
            {
                handle = ZipFacade.getZipHandle(getArtifact());

                final String embeddedPomPath =
                        "META-INF/maven/" + getGav().getGroupId() + "/" + getGav().getArtifactId() + "/pom.xml";

                if (handle.hasEntry(embeddedPomPath))
                {
                    try (InputStream is = handle.getEntryContent(embeddedPomPath))
                    {
                        return new MavenXpp3Reader().read(is, false);
                    }

                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (XmlPullParserException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    ZipFacade.close(handle);
                }
                catch (Exception e)
                {
                }
            }
        }

        return null;
    }
}
