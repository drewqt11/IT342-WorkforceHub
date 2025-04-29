package cit.edu.workforce.Utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * Custom Hibernate ID generator for Reimbursement Request IDs.
 * New file: Generates IDs in the format "REIM-XXXX-XXXXX" where X is a random hexadecimal character (0-9, a-f).
 */
public class ReimbursementRequestIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return CustomIdGenerator.generateReimbursementRequestId();
    }
} 