package cit.edu.workforce.Utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * Custom Hibernate ID generator for Employee IDs.
 * Generates IDs in the format "EMPX-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
 */
public class EmployeeIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return CustomIdGenerator.generateEmployeeId();
    }
}
