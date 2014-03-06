package org.carlspring.strongbox.visitors;

import org.carlspring.strongbox.security.jaas.Group;

import java.util.Set;

/**
 * @author mtodorov
 */
public interface Visitor
{

    void visit(Group group, Set<Group> hierarchy);

    void endVisit(Group group, Set<Group> hierarchy);

}
