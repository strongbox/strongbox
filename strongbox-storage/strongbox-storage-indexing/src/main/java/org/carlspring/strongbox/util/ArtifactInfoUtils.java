package org.carlspring.strongbox.util;

import org.apache.maven.index.ArtifactInfo;

/**
 * @author mtodorov
 */
public class ArtifactInfoUtils
{

    private ArtifactInfoUtils() {
    }

    public static String convertToGAVTC(ArtifactInfo artifactInfo)
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String gavtc = artifactInfo.getGroupId() + ":" +
                       artifactInfo.getArtifactId() + ":" +
                       artifactInfo.getVersion() + ":" +
                       artifactInfo.getPackaging() + ":" +
                       artifactInfo.getClassifier();

        return gavtc;
    }

}
