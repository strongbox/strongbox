/*
 * Copyright 2019 Carlspring Consulting & Development Ltd.
 * Copyright 2014 Dmitry Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carlspring.strongbox.storage.metadata.nuget;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.db.schema.Properties;
import org.carlspring.strongbox.storage.metadata.nuget.rss.PackageFeed;

/**
 *
 * @author Unlocker
 */
@XmlRootElement(name = "author", namespace = PackageFeed.ATOM_XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public class Author
{

    @XmlElement(name = Properties.NAME, namespace = PackageFeed.ATOM_XML_NAMESPACE)
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Author()
    {
    }

    public Author(String name)
    {
        this.name = name;
    }
}
