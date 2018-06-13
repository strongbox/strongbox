package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.routing.MutableRuleSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class RuleSetMapAdapter
        extends XmlAdapter<RuleSetMap, Map<String, MutableRuleSet>>
{

    @Override
    public RuleSetMap marshal(Map<String, MutableRuleSet> map)
            throws Exception
    {
        RuleSetMap ruleSetMap = new RuleSetMap();
        for (Map.Entry<String, MutableRuleSet> entry : map.entrySet())
        {
            ruleSetMap.getEntries().add(entry.getValue());
        }

        return ruleSetMap;
    }

    @Override
    public Map<String, MutableRuleSet> unmarshal(RuleSetMap ruleSetMap)
            throws Exception
    {
        List<MutableRuleSet> entries = ruleSetMap.getEntries();

        Map<String, MutableRuleSet> map = new LinkedHashMap<>(entries.size());
        for (MutableRuleSet routingRule : entries)
        {
            map.put(routingRule.getGroupRepository(), routingRule);
        }

        return map;
    }

}
