package cit.edu.workforce.Utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Random;

/**
 * EventIdGenerator - Generates custom IDs for Event entities
 * New file: Generates IDs in the format "EV" + random 14-digit number
 */
public class EventIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("EV");
        
        // Generate 14 random digits
        for (int i = 0; i < 14; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
} 