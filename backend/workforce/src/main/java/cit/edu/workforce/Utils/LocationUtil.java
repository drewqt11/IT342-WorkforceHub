package cit.edu.workforce.Utils;

import org.springframework.stereotype.Component;

@Component
public class LocationUtil {

    private static final double EARTH_RADIUS = 6371e3; // Earth radius in meters

    /**
     * Calculates the distance between two coordinates using the Haversine formula
     * @param lat1 Latitude of first point in degrees
     * @param lon1 Longitude of first point in degrees
     * @param lat2 Latitude of second point in degrees
     * @param lon2 Longitude of second point in degrees
     * @return Distance in meters
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);

        // Differences
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in meters
    }

    /**
     * Checks if a location is within the allowed radius of an office location
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param officeLat Office latitude
     * @param officeLon Office longitude
     * @param allowedRadius Allowed radius in meters
     * @return true if within boundary, false otherwise
     */
    public boolean isWithinBoundary(double userLat, double userLon, double officeLat, double officeLon, double allowedRadius) {
        double distance = calculateDistance(userLat, userLon, officeLat, officeLon);
        return distance <= allowedRadius;
    }
} 