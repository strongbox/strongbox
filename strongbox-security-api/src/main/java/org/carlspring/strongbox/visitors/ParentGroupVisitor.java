package org.carlspring.strongbox.visitors;

import org.carlspring.strongbox.security.jaas.Group;
import org.carlspring.strongbox.security.jaas.authentication.NotSupportedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author mtodorov
 */
public class ParentGroupVisitor implements Visitor
{


    @Override
    public void visit(Group group, Set<Group> hierarchy) throws NotSupportedException
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
    public void endVisit(Group group, Set<Group> hierarchy)
    {
        // Invert the list, so it's top to bottom instead.
        List<Group> list = new ArrayList<Group>(hierarchy);
        Collections.reverse(list);

        hierarchy.clear();
        hierarchy.addAll(list);
    }

}
