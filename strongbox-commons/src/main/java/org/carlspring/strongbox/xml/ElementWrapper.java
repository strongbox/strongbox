package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ElementWrapper<T>
{

    @XmlAnyElement(lax = true)
    private List<T> elements = new ArrayList<T>();


    public ElementWrapper()
    {
    }

    public ElementWrapper(List<T> elements)
    {
        this.elements = elements;
    }

    public List<T> getElements()
    {
        return elements;
    }

    public void setElements(List<T> elements)
    {
        this.elements = elements;
    }

}
