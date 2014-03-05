package org.carlspring.strongbox.visitors;

import org.carlspring.strongbox.security.jaas.Group;

import java.util.Collections;
import java.util.List;

/**
 * @author mtodorov
 */
public class ParentGroupVisitor implements Visitor
{


    @Override
    public void visit(Group group, List<Group> hierarchy)
    {
        if (group.getParent() != null)
        {
            hierarchy.add(group);
            visit(group.getParent(), hierarchy);
        }
        else
        {
            hierarchy.add(group);
            endVisit(group, hierarchy);
        }
    }

    @Override
    public void endVisit(Group group, List<Group> hierarchy)
    {
        // Invert the list, so it's top to bottom instead.
        Collections.reverse(hierarchy);
    }

}
