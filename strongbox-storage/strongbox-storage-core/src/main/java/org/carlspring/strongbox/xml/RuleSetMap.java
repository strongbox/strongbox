package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.routing.MutableRuleSet;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class RuleSetMap
{

    @XmlElement(name = "rule-set")
    private List<MutableRuleSet> entries = new ArrayList<>();


    public List<MutableRuleSet> getEntries()
    {
        return entries;
    }

    public void add(MutableRuleSet routingRule)
    {
        entries.add(routingRule);
    }

}

