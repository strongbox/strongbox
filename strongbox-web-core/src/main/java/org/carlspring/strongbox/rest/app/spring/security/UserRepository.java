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
public class UserRepository {

	@Autowired
	private PasswordEncoder passwordEncoder;

	public StrongboxUser findByUserName(final String username) {

		// dummy impl that "finds" only user with username/password: strongbox/strongbox
		StrongboxUser user;
		if ("strongbox".equals(username)) {
			user = new StrongboxUser();
			user.setEnabled(true);
			user.setUsername(username);
			String password = passwordEncoder.encode(username);
			user.setPassword(password);
		}

		user = withDatabase(db -> {
			OSQLSynchQuery<StrongboxUser> query = new OSQLSynchQuery<>("SELECT * FROM StrongboxUser WHERE username = '"+username+"'");
			List<StrongboxUser> result = db.query(query);
			if (!result.isEmpty()) {
				StrongboxUser strongboxUser = result.get(0);
				strongboxUser = db.detach(strongboxUser, true);
				return strongboxUser;
			}
			return null;
		});

		return user;
	}

	@PostConstruct
	public void postConstruct() {
		withDatabase(db -> {
			db.getEntityManager().registerEntityClass(StrongboxUser.class);

			StrongboxUser martin = db.newInstance(StrongboxUser.class);
			martin.setUsername("martin" );
			martin.setPassword( passwordEncoder.encode("agent007"));
			martin.setRoles(Collections.singletonList("ROLE_ADMIN"));
			martin.setEnabled(true);

			db.save(martin);
			return null;
		});
	}

	private <R> R withDatabase(Function<OObjectDatabaseTx, R> code) {
		OObjectDatabaseTx db = new OObjectDatabaseTx("memory:strongbox");
		try {
			if (db.exists()) {
				ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
				db.open("admin", "admin");
			} else {
				db.create();
			}
			return code.apply(db);
		} finally {
			if (!db.isClosed()) {
				db.close();
			}
		}
	}
}
