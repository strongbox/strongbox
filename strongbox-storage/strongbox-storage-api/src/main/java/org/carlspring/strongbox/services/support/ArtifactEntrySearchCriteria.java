package org.carlspring.strongbox.services.support;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactEntrySearchCriteria
{

    private Integer uselessnessDays;

    private Integer minSizeInBytes;


    public static final class Builder
    {

        private Integer uselessnessDays;
        private Integer minSizeInBytes;

        private Builder()
        {
        }

        public static Builder anArtifactEntrySearchCriteria()
        {
            return new Builder();
        }

        public Builder withUselessnessDays(Integer uselessnessDays)
        {
            this.uselessnessDays = uselessnessDays;
            return this;
        }

        public Builder withMinSizeInBytes(Integer minSizeInBytes)
        {
            this.minSizeInBytes = minSizeInBytes;
            return this;
        }

        public ArtifactEntrySearchCriteria build()
        {
            ArtifactEntrySearchCriteria artifactEntrySearchCriteria = new ArtifactEntrySearchCriteria();
            artifactEntrySearchCriteria.uselessnessDays = this.uselessnessDays;
            artifactEntrySearchCriteria.minSizeInBytes = this.minSizeInBytes;
            return artifactEntrySearchCriteria;
        }
    }
}
