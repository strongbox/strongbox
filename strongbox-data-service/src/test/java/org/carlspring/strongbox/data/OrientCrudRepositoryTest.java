package org.carlspring.strongbox.data;

import org.carlspring.strongbox.data.config.DataServiceConfig;
import org.carlspring.strongbox.data.domain.StrongboxUser;
import org.carlspring.strongbox.data.service.StrongboxUserService;

import javax.inject.Inject;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DataServiceConfig.class })
public class OrientCrudRepositoryTest
{

    private static final Logger logger = LoggerFactory.getLogger(OrientCrudRepositoryTest.class);

    @Inject
    StrongboxUserService userService;

    @Test
    @Transactional
    public void testCreateAndDeleteUserOperations()
            throws Exception
    {

        // remove test user if already exists
        userService.findByUserName("test").ifPresent(user -> userService.delete(user));

        final StrongboxUser user = new StrongboxUser();
        user.setEnabled(true);
        user.setUsername("test");
        user.setPassword("test-pwd");
        userService.save(user);

        Optional<StrongboxUser> optional = userService.findByUserName("test");
        optional.ifPresent(storedUser -> {
            logger.debug("Found stored user\n\t" + storedUser + "\n");
            assertEquals(user.getUsername(), storedUser.getUsername());
            assertEquals(user.getPassword(), storedUser.getPassword());
            assertEquals(user.isEnabled(), storedUser.isEnabled());
        });
        optional.orElseThrow(NullPointerException::new);

        userService.delete(user);
    }

    @Test
    public void displayUsers()
    {
        userService.findAll().forEach(user -> logger.debug(user.toString()));
    }
}
