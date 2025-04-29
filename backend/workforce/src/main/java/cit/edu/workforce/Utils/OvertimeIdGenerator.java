package cit.edu.workforce.Utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Random;

/**
 * OvertimeIdGenerator - Custom ID generator for overtime requests
 * New file: Generates IDs for overtime requests in the format OT-XXXXXXXXXX
 */
public class OvertimeIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("OT-");
        
        // Generate a 10-character random alphanumeric string
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 10; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }
} 