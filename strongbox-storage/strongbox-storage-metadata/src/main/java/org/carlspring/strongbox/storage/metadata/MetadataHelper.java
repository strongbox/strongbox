package org.carlspring.strongbox.storage.metadata;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author mtodorov
 */
public class MetadataHelper
{

    public static final SimpleDateFormat LAST_UPDATED_FIELD_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");


    public static void setLastUpdated(Versioning versioning)
    {
        if (versioning != null)
        {
            versioning.setLastUpdated(LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime()));
        }
    }

    public static void setLatest(Metadata metadata)
    {
        setLatest(metadata, null);
    }

    /**
     * Sets the "latest" field.
     *
     * @param metadata          The metadata to apply this to.
     * @param currentLatest     Only pass this in, if this is delete mode, otherwise
     *                          this method will figure things out on it's own.
     */
    public static void setLatest(Metadata metadata, String currentLatest)
    {
        Versioning versioning = metadata.getVersioning() != null ? metadata.getVersioning() : new Versioning();
        if (metadata.getVersioning() == null)
        {
            metadata.setVersioning(versioning);
        }

        List<String> versions = versioning.getVersions();

        if (versioning.getLatest() == null)
        {
            versioning.setLatest(versions.get(versions.size() - 1));

            return;
        }

        // Delete mode:
        if (currentLatest != null && versioning.getLatest().equals(currentLatest))
        {
            if (versions.size() > 1)
            {
                // TODO: Is this the right thing to do?
                versioning.setLatest(versions.get(versions.size() - 2));
            }
            else
            {
                // TODO: Figure out what we should do in case there are no other available versions
            }
        }
    }

    /**
     * @param metadata          The metadata to apply this to.
     */
    public static void setRelease(Metadata metadata)
    {
        setRelease(metadata, null);
    }

    /**
     * Sets the "release" field.
     *
     * @param metadata          The metadata to apply this to.
     * @param currentRelease    Only pass this in, if this is delete mode, otherwise
     *                          this method will figure things out on it's own.
     */
    public static void setRelease(Metadata metadata, String currentRelease)
    {
        Versioning versioning = metadata.getVersioning() != null ? metadata.getVersioning() : new Versioning();
        if (metadata.getVersioning() == null)
        {
            metadata.setVersioning(versioning);
        }

        List<String> versions = versioning.getVersions();

        if (versioning.getRelease() == null)
        {
            versioning.setRelease(versions.get(versions.size() - 1));

            return;
        }

        // Delete mode:
        if (currentRelease != null && versioning.getRelease().equals(currentRelease) && versioning.getRelease().equals(currentRelease))
        {
            if (versions.size() > 1)
            {
                versioning.setRelease(versions.get(versions.size() - 2));
            }
            else
            {
                // TODO: Figure out what we should do in case there are no other available versions
            }
        }
    }

}
