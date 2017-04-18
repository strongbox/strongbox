package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class RuleSetMap
{

    @XmlElement(name = "rule-set")
    private List<RuleSet> entries = new ArrayList<>();


    public List<RuleSet> getEntries()
    {
        return entries;
    }

    public void add(RuleSet routingRule)
    {
        entries.add(routingRule);
    }

}

