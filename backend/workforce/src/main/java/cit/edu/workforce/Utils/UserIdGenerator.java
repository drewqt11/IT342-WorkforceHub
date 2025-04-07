package cit.edu.workforce.Utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * Custom Hibernate ID generator for User IDs.
 * Generates IDs in the format "USER-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
 */
public class UserIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return CustomIdGenerator.generateUserId();
    }
}
