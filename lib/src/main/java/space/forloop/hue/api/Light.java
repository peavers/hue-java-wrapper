package space.forloop.hue.api;

import space.forloop.hue.exception.HueException;
import space.forloop.hue.model.LightCapabilities;
import space.forloop.hue.model.LightState;

/** Represents a Hue light bulb and its capabilities. */
public interface Light {
    /**
     * @return The light's unique identifier
     */
    String getId();

    /**
     * @return The light's current state
     */
    LightState getState() throws HueException;

    /**
     * Updates the light's state.
     *
     * @param state New state to apply
     * @throws HueException if update fails
     */
    void setState(LightState state) throws HueException;

    /**
     * @return The light's capabilities
     */
    LightCapabilities getCapabilities() throws HueException;
}
