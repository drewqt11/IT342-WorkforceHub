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
        if ("microsoft".equals(registrationId)) {
            if (attributes.containsKey("givenName")) {
                return (String) attributes.get("givenName");
            }
        }
        return (String) attributes.get("given_name");
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
} 