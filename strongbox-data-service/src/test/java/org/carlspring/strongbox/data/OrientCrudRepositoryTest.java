package org.carlspring.strongbox.data;

import org.carlspring.strongbox.data.domain.StrongboxUser;
import org.carlspring.strongbox.data.service.StrongboxUserService;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@DataServiceTestContext
@RunWith(SpringJUnit4ClassRunner.class)
public class OrientCrudRepositoryTest
{

    private static final Logger logger = LoggerFactory.getLogger(OrientCrudRepositoryTest.class);

    final String testUserName = "TEST";

    @Autowired
    StrongboxUserService userService;

    @Before
    public void setup()
    {
        assertNotNull(userService);
    }

    @Test
    @Transactional
    public void testCreateAndDeleteUserOperations()
            throws Exception
    {

        final StrongboxUser user = new StrongboxUser();
        user.setEnabled(true);
        user.setUsername(testUserName);
        user.setPassword("test-pwd");
        final StrongboxUser savedUser = userService.save(user);
        assertNotNull(savedUser);

        String id = savedUser.getId();
        System.out.println("\n\n" + savedUser + "\n\n");

        Optional<StrongboxUser> optional = userService.findOne(id);
        optional.ifPresent(storedUser -> {
            logger.debug("Found stored user\n\t" + storedUser + "\n");
            assertEquals(user.getUsername(), storedUser.getUsername());
            assertEquals(user.getPassword(), storedUser.getPassword());
            assertEquals(user.isEnabled(), storedUser.isEnabled());
        });
        optional.orElseThrow(
                () -> new NullPointerException("Unable to locate user " + testUserName + ". Save operation fails..."));
    }

    @Test
    @Transactional
    public void displayUsers()
    {
        userService.findAll().ifPresent(strongboxUsers -> {
            strongboxUsers.forEach(user -> logger.debug(user.toString()));
        });
    }
}
