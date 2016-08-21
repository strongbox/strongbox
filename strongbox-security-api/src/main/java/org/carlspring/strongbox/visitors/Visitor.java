package org.carlspring.strongbox.visitors;

import org.carlspring.strongbox.security.Group;
import org.carlspring.strongbox.security.exceptions.NotSupportedException;

import java.util.Set;

/**
 * @author mtodorov
 */
public interface Visitor
{

    void visit(Group group, Set<Group> hierarchy) throws NotSupportedException;

    void endVisit(Group group, Set<Group> hierarchy);

}
