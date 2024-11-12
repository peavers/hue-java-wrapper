package space.forloop.hue.api;

import java.util.Collection;

import space.forloop.hue.exception.HueException;

/**
 * Represents an authenticated session with a Hue bridge. Provides access to lights, groups, scenes,
 * and other bridge features.
 */
public interface AuthenticatedBridge {
    /**
     * Find all the lights attached to the bridge
     *
     * @return Collection of all lights connected to the bridge
     * @throws HueException if retrieval fails
     */
    Collection<Light> getLights() throws HueException;
}
