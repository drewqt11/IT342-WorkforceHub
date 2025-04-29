package cit.edu.workforce.Utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Random;

/**
 * ImprovementPlanIdGenerator - Generates custom IDs for Improvement Plan entities
 * New file: Generates IDs in the format "PIP" + random 13-digit number
 */
public class ImprovementPlanIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("PIP");
        
        // Generate 13 random digits
        for (int i = 0; i < 13; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
} 