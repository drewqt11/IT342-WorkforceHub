package cit.edu.workforce.Security;

import java.util.Map;

public class OAuth2UserInfo {
    protected Map<String, Object> attributes;
    protected String registrationId;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this(attributes, null);
    }
    
    public OAuth2UserInfo(Map<String, Object> attributes, String registrationId) {
        this.attributes = attributes;
        this.registrationId = registrationId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getId() {
        return (String) attributes.get("sub");
    }

    public String getName() {
        return (String) attributes.get("name");
    }

    public String getEmail() {
        if ("microsoft".equals(registrationId)) {
            // Microsoft might use different keys
            if (attributes.containsKey("userPrincipalName")) {
                return (String) attributes.get("userPrincipalName");
            } else if (attributes.containsKey("mail")) {
                return (String) attributes.get("mail");
            }
        }
        return (String) attributes.get("email");
    }

    public String getFirstName() {
        String givenName = null;
        if ("microsoft".equals(registrationId)) {
            if (attributes.containsKey("given_name")) {
                givenName = (String) attributes.get("given_name");
            }
        }
        if (givenName == null) {
            givenName = (String) attributes.get("given_name");
        }
        
        if (givenName != null && givenName.matches(".*\\d+.*")) {
            String[] parts = extractIdNumberAndName(givenName);
            return parts[1]; // Return only the name part
        }
        
        return givenName;
    }

    public String getLastName() {
        if ("microsoft".equals(registrationId)) {
            if (attributes.containsKey("surname")) {
                return (String) attributes.get("surname");
            }
        }
        return (String) attributes.get("family_name");
    }

    public String getImageUrl() {
        return (String) attributes.get("picture");
    }

    public String getIdNumber() {
        String givenName = null;
        if ("microsoft".equals(registrationId)) {
            if (attributes.containsKey("given_name")) {
                givenName = (String) attributes.get("given_name");
            }
        }
        if (givenName == null) {
            givenName = (String) attributes.get("given_name");
        }
        
        if (givenName != null && givenName.matches(".*\\d+.*")) {
            String[] parts = extractIdNumberAndName(givenName);
            return parts[0]; // Return only the ID number part
        }
        
        return "";
    }

    private String[] extractIdNumberAndName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[]{"", ""};
        }

        input = input.trim();
        
        // Split by whitespace
        String[] parts = input.split("\\s+", 2);
        
        if (parts.length == 2) {
            String potentialIdNumber = parts[0];
            String name = parts[1];
            
            // Check if the first part matches ID number format (numbers and dashes)
            if (potentialIdNumber.matches("\\d+(-\\d+)+")) {
                return new String[]{potentialIdNumber, name};
            }
        }
        
        // If no valid ID number found, return the original input as the name
        return new String[]{"", input};
    }
} 