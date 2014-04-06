package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import static junit.framework.Assert.assertFalse;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author stodorov
 */
public class RepositoryGroupValidatorTest
{

    @Test
    public void testGroupRepositoryValidation()
    {

        Repository repository = new Repository();
        RepositoryGroupValidator validator = new RepositoryGroupValidator();

        String groupType = RepositoryTypeEnum.GROUP.toString();

        // Normal repositories should assert false
        assertFalse("Expected false, got true when validating if repository type is " + groupType,
                    validator.validate(repository));

        // Group repositories should assert true
        repository.setType(RepositoryTypeEnum.GROUP.toString());
        assertTrue("Expected true, got false when validating if repository type is " + groupType,
                   validator.validate(repository));

    }

}
