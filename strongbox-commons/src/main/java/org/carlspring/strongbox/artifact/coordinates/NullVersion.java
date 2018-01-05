package org.carlspring.strongbox.artifact.coordinates;

/**
 * @author carlspring
 */
public class NullVersion
        implements Comparable<NullVersion>
{

    @Override
    public int compareTo(NullVersion o)
    {
        // Since raw repositories have no version, all versions are the same, hence:
        return 0;
    }

}
