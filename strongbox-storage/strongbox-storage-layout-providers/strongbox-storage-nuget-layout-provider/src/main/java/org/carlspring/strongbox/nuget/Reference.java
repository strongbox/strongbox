package org.carlspring.strongbox.nuget;

import java.io.Serializable;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * File reference
 */
public class Reference implements Serializable {

    /**
     * File name
     */
    @XmlAttribute(name = "file")
    private String file;

    /**
     * @param file new file name
     * @return this instance.
     */
    public Reference setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Reference)) {
            return false;
        }
        Reference o = (Reference) obj;
        return Objects.equals(o.file, this.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.file);
    }
}
