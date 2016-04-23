package org.carlspring.strongbox.rest.app.spring.security;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * DAO for {@link StrongboxUser} entities
 */
@Repository
public class UserRepository
{

    @Autowired
    private PasswordEncoder passwordEncoder;

    public StrongboxUser findByUserName(final String username)
    {

        StrongboxUser user = withDatabase(db -> {
            // TODO Does OrientDB support parameters ?!
            OSQLSynchQuery<StrongboxUser> query = new OSQLSynchQuery<>("SELECT *" +
                                                                       "  FROM StrongboxUser" +
                                                                       " WHERE username = '" + username + "'");
            List<StrongboxUser> result = db.query(query);
            if (!result.isEmpty())
            {
                StrongboxUser strongboxUser = result.get(0);
                strongboxUser = db.detach(strongboxUser, true);

                return strongboxUser;
            }

            return null;
        });

        return user;
    }

    @PostConstruct
    public void postConstruct()
    {
        // add a user for testing
        // Usually OrientDB should be setup separately(i.e.remotely) and
        // users should be added either via REST endpoint or via OrientDB Studio/Console
        withDatabase(db -> {
            db.getEntityManager().registerEntityClass(StrongboxUser.class);

            StrongboxUser user = db.newInstance(StrongboxUser.class);
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRoles(Collections.singletonList("ROLE_ADMIN"));
            user.setEnabled(true);

            db.save(user);

            return null;
        });
    }

    private <R> R withDatabase(Function<OObjectDatabaseTx, R> code)
    {
        // for simplicity use inmemory database
        // TODO Replace it with remote instance!
        OObjectDatabaseTx db = new OObjectDatabaseTx("memory:strongbox");
        try
        {
            if (db.exists())
            {
                ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
                db.open("admin", "admin");
            }
            else
            {
                db.create();
            }
            return code.apply(db);
        }
        finally
        {
            if (!db.isClosed())
            {
                db.close();
            }
        }
    }

}
