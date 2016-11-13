package org.carlspring.strongbox.artifact.coordinates;

/**
 * Represents {@link ArtifactCoordinates} for P2 repository
 *
 * Proper path for this coordinates is in the format of: {id}/{version}/{classifier}
 * Example: strongbox.p2/1.0.0/osgi.bundle
 */
public class P2ArtifactCoordinates extends AbstractArtifactCoordinates {

    public static final String ID = "id";

    public static final String VERSION = "version";

    public static final String CLASSIFIER = "classifier";

    private static final String SEPARATOR = "/";

    public P2ArtifactCoordinates(String id, String version, String classifier) {
        if (id == null || version == null || classifier == null) {
            throw new IllegalArgumentException("Id, version and classifier must be specified");
        }
        setId(id);
        setVersion(version);
        setCoordinate(CLASSIFIER, classifier);
    }

    @Override
    public String getId() {
        return getCoordinate(ID);
    }

    @Override
    public void setId(String id) {
        setCoordinate(ID, id);
    }

    @Override
    public String getVersion() {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version) {
        setCoordinate(VERSION, version);
    }

    public String getClassifier() {
        return getCoordinate(CLASSIFIER);
    }

    @Override
    public String toPath() {
        return getId() + SEPARATOR + getVersion() + SEPARATOR + getClassifier();
    }

    public String getFilename() {
        return "plugins/" + getId() + "_" + getVersion() + ".jar";
    }

    public static P2ArtifactCoordinates create(String path) {
        if (path != null && path.length() > 0 && path.contains(SEPARATOR)) {
            String[] splitedSeparator = path.split("/");
            if (splitedSeparator.length == 3) {
                return new P2ArtifactCoordinates(splitedSeparator[0], splitedSeparator[1], splitedSeparator[2]);
            }
        }

        throw new IllegalArgumentException("Path is not properly formatted");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        P2ArtifactCoordinates that = (P2ArtifactCoordinates) o;

        if (!getId().equals(that.getId())) return false;
        if (!getVersion().equals(that.getVersion())) return false;
        return getClassifier().equals(that.getClassifier());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getVersion().hashCode();
        result = 31 * result + getClassifier().hashCode();
        return result;
    }
}
