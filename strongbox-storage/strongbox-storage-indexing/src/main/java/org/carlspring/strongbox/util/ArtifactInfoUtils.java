package org.carlspring.strongbox.util;

import org.apache.maven.index.ArtifactInfo;

/**
 * @author mtodorov
 */
public class ArtifactInfoUtils
{

    public static String convertToGAVTC(ArtifactInfo artifactInfo)
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String gavtc = artifactInfo.groupId + ":" +
                       artifactInfo.artifactId + ":" +
                       artifactInfo.version + ":" +
                       artifactInfo.packaging + ":" +
                       artifactInfo.classifier;

        return gavtc;
    }

}
