package com.example.anchornotes.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Service that encapsulates access to the device's location providers.
 * Presentation/Domain code should talk to this instead of directly to
 * LocationManager or permission APIs.
 */
public class LocationProviderService {

    public enum LocationError {
        NONE,
        PERMISSION_MISSING,
        PROVIDER_DISABLED,
        LOCATION_UNAVAILABLE
    }

    private final Context appContext;
    private final LocationManager locationManager;

    public LocationProviderService(Context context) {
        this.appContext = context.getApplicationContext();
        this.locationManager =
                (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Check if we currently have either FINE or COARSE location permission.
     */
    public boolean isLocationPermissionGranted() {
        int fine = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        int coarse = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
        );
        return fine == PackageManager.PERMISSION_GRANTED
                || coarse == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check whether location is "available" in principle (permission + provider).
     * This does not guarantee a non-null Location, but it tells you whether it
     * even makes sense to try to fetch one.
     */
    public LocationError getAvailabilityStatus() {
        if (!isLocationPermissionGranted()) {
            return LocationError.PERMISSION_MISSING;
        }
        if (locationManager == null) {
            return LocationError.PROVIDER_DISABLED;
        }

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) { }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) { }

        if (!gpsEnabled && !networkEnabled) {
            return LocationError.PROVIDER_DISABLED;
        }

        return LocationError.NONE;
    }

    /**
     * Return a "best-effort" last known location, or null if none is available.
     * Caller is responsible for checking permission and provider availability
     * (getAvailabilityStatus) before calling this.
     */
    @Nullable
    public Location getLastKnownLocation() {
        if (locationManager == null) {
            return null;
        }

        Location best = null;

        try {
            Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gps != null) {
                best = gps;
            }
        } catch (SecurityException ignored) { }

        try {
            Location net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (net != null && best == null) {
                best = net;
            }
        } catch (SecurityException ignored) { }

        return best;
    }
}
