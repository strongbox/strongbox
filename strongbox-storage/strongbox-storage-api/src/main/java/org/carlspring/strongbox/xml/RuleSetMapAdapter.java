package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class RuleSetMapAdapter
        extends XmlAdapter<RuleSetMap, Map<String, RuleSet>>
{

    @Override
    public RuleSetMap marshal(Map<String, RuleSet> map)
            throws Exception
    {
        RuleSetMap ruleSetMap = new RuleSetMap();
        for (Map.Entry<String, RuleSet> entry : map.entrySet())
        {
            ruleSetMap.getEntries().add(entry.getValue());
        }

        return ruleSetMap;
    }

    @Override
    public Map<String, RuleSet> unmarshal(RuleSetMap ruleSetMap)
            throws Exception
    {
        List<RuleSet> entries = ruleSetMap.getEntries();

        Map<String, RuleSet> map = new LinkedHashMap<>(entries.size());
        for (RuleSet routingRule : entries)
        {
            map.put(routingRule.getGroupRepository(), routingRule);
        }

        return map;
    }

}