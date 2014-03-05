package org.carlspring.strongbox.visitors;

import org.carlspring.strongbox.security.jaas.Group;

import java.util.List;

/**
 * @author mtodorov
 */
public interface Visitor
{

    void visit(Group group,
               List<Group> hierarchy);

    void endVisit(Group group,
                  List<Group> hierarchy);

}
