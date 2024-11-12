package space.forloop.hue.model;

import java.util.Optional;

import lombok.Builder;

/**
 * Represents the current state or desired state of a Philips Hue light. This record is used both
 * for reading the current state and setting a new state.
 *
 * @param on Whether the light is turned on (true) or off (false)
 * @param brightness The brightness level of the light (typically 0-254)
 * @param colorTemperature The color temperature in mireds (typically 153-500)
 * @param color The color settings of the light, if supported
 */
@Builder
public record LightState(
        /** Whether the light is turned on (true) or off (false) */
        boolean on,

        /** The brightness level of the light (typically 0-254) */
        Integer brightness,

        /** The color temperature in mireds (typically 153-500) */
        Integer colorTemperature,

        /** The color settings of the light, if supported */
        Optional<Color> color) {}
