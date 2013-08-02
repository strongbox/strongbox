package org.carlspring.repositoryunit.annotations;

/**
 * @author mtodorov
 */
public enum ArtifactExistenceState
{

    NOT_FOUND ("NOT_FOUND"), EXISTS("EXISTS"), RELOCATED("RELOCATED");

    private String state;

    private ArtifactExistenceState(String state)
    {
        this.state = state;
    }

    public String getState()
    {
        return state;
    }

}
