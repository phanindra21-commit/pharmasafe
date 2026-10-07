package com.pharmasafe.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Hyderabad localities the pharmacist can search from, with approximate centre coordinates.
 * Distances to pharmacies are measured from these points.
 */
@Component
public class AreaDirectory {

    public record Area(String name, double latitude, double longitude) { }

    private static final List<Area> AREAS = List.of(
            new Area("Ameerpet", 17.4375, 78.4483),
            new Area("Banjara Hills", 17.4156, 78.4347),
            new Area("Begumpet", 17.4447, 78.4664),
            new Area("Charminar", 17.3616, 78.4747),
            new Area("Dilsukhnagar", 17.3688, 78.5247),
            new Area("Gachibowli", 17.4401, 78.3489),
            new Area("Jubilee Hills", 17.4326, 78.4071),
            new Area("Kompally", 17.5390, 78.4860),
            new Area("Kondapur", 17.4600, 78.3600),
            new Area("Kukatpally", 17.4948, 78.3996),
            new Area("LB Nagar", 17.3457, 78.5522),
            new Area("Madhapur", 17.4483, 78.3915),
            new Area("Malkajgiri", 17.4532, 78.5266),
            new Area("Mehdipatnam", 17.3950, 78.4410),
            new Area("Miyapur", 17.4968, 78.3614),
            new Area("Secunderabad", 17.4399, 78.4983),
            new Area("Tarnaka", 17.4290, 78.5400),
            new Area("Uppal", 17.4058, 78.5591));

    public List<Area> all() {
        return AREAS;
    }

    /** Case-insensitive lookup by name. */
    public Optional<Area> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String wanted = name.trim();
        return AREAS.stream().filter(a -> a.name().equalsIgnoreCase(wanted)).findFirst();
    }

    /** Great-circle distance in kilometres (haversine formula). */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
