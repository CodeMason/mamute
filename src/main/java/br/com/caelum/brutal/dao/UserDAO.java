package br.com.caelum.brutal.dao;

import net.vidageek.mirror.dsl.Mirror;

import org.hibernate.Query;
import org.hibernate.Session;

import br.com.caelum.brutal.infra.Digester;
import br.com.caelum.brutal.model.User;
import br.com.caelum.vraptor.ioc.Component;

@Component
public class UserDAO {

	private final Session session;

	public UserDAO(Session session) {
		this.session = session;
	}

	public User findByMailAndPassword(String email, String pass) {
		return findByEmailAndEncryptedPassword(email, Digester.encrypt(pass));
	}

	public void save(User user) {
		session.save(user);
	}

	public User findById(Long id) {
		return (User) session.load(User.class, id);
	}

	public User load(User user) {
		if (user == null)
			return null;
		return findById(user.getId());
	}

	public User loadByEmail(String email) {
	    if (email == null) {
	        throw new IllegalArgumentException("impossible to search for a null email");
	    }
		return (User) session
				.createQuery("from User where email = :email")
				.setParameter("email", email)
				.uniqueResult();
	}

	private User loadByName(String name) {
	    if (name == null) {
	        throw new IllegalArgumentException("impossible to search for a null name");
	    }
	    
	    return (User) session
	    		.createQuery("from User where name = :name")
	    		.setParameter("name", name)
	    		.uniqueResult();
	}
	
	public User loadByIdAndToken(Long id, String token) {
		return (User) session
				.createQuery("from User where id = :id and forgotPasswordToken = :token")
				.setParameter("id", id)
				.setParameter("token", token)
				.uniqueResult();
	}

    public boolean existsWithEmail(String email) {
        return loadByEmail(email) != null;
    }

    public User findByMailAndLegacyPasswordAndUpdatePassword(String email, String password) {
        User legacyUser = findByEmailAndEncryptedPassword(email, Digester.legacyMd5(password));
        if (legacyUser != null) {
            new Mirror().on(legacyUser).set().field("password").withValue(Digester.encrypt(password));
        }
        return legacyUser;
    }
    
    private User findByEmailAndEncryptedPassword(String email, String pass) {
        return (User) session.createQuery("from User where email = :email and password = :password")
                .setParameter("email", email)
                .setParameter("password", pass)
                .uniqueResult();
    }

	public boolean existsWithName(String name) {
		return loadByName(name) != null;	
	}

    public User findBySessionKey(String key) {
        Query query = session.createQuery("from User where sessionKey=:key");
        return (User) query.setParameter("key", key).uniqueResult();
        
    }
}
