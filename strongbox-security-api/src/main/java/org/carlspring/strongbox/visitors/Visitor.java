package org.carlspring.strongbox.visitors;

import org.carlspring.strongbox.security.jaas.Group;
import org.carlspring.strongbox.security.jaas.authentication.NotSupportedException;

import java.util.Set;

/**
 * @author mtodorov
 */
public interface Visitor
{

    void visit(Group group, Set<Group> hierarchy) throws NotSupportedException;

    void endVisit(Group group, Set<Group> hierarchy);

}
