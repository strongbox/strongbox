package org.carlspring.strongbox.test.domain;

import javax.persistence.Entity;

import org.carlspring.strongbox.data.domain.GenericEntity;

@Entity
public class FooEntity extends GenericEntity
{

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
