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
 *
 * For this class project, if no real last-known location is available,
 * we fall back to a fixed mock coordinate so the feature still works.
 */
public class LocationProviderService {

    public enum LocationError {
        OK,
        PERMISSION_MISSING,
        PROVIDER_DISABLED
    }

    private final Context appContext;
    private final LocationManager locationManager;

    public LocationProviderService(Context context) {
        this.appContext = context.getApplicationContext();
        this.locationManager =
                (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Quick pre-check for whether we’re allowed/able to use location.
     */
    public LocationError getAvailabilityStatus() {
        // Runtime permission check
        if (ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
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
            // Neither major provider is enabled
            return LocationError.PROVIDER_DISABLED;
        }

        return LocationError.OK;
    }

    /**
     * Returns the best last known location from the system, or a mock
     * fallback if none is available (so the app keeps working on emulators).
     * Caller MUST have already checked for permission.
     */
    @Nullable
    public Location getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        if (locationManager == null) {
            return createMockFallbackLocation();
        }

        Location best = null;

        best = tryGetLastKnown(LocationManager.GPS_PROVIDER);
        if (best != null) {
            return best;
        }

        best = tryGetLastKnown(LocationManager.NETWORK_PROVIDER);
        if (best != null) {
            return best;
        }

        best = tryGetLastKnown(LocationManager.PASSIVE_PROVIDER);
        if (best != null) {
            return best;
        }

        // On some emulators this can still be null, so we provide a fixed fallback.
        return createMockFallbackLocation();
    }

    @Nullable
    private Location tryGetLastKnown(String provider) {
        try {
            return locationManager.getLastKnownLocation(provider);
        } catch (SecurityException ignored) {
            return null;
        } catch (IllegalArgumentException ignored) {
            // Provider not present on this device
            return null;
        }
    }

    /**
     * Fallback coordinate used when the system has no last-known location.
     * Here we just pick a reasonable fixed point (e.g., USC).
     */
    private Location createMockFallbackLocation() {
        Location mock = new Location("mock");
        mock.setLatitude(34.0219);   // example: USC latitude
        mock.setLongitude(-118.2851); // example: USC longitude
        return mock;
    }
}
