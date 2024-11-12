package space.forloop.hue.model;

import java.util.Optional;

/**
 * Describes the capabilities of a Philips Hue light, including supported features and their
 * operational ranges. This information is useful for determining what operations can be performed
 * on a specific light.
 *
 * @param supportsColor Indicates whether the light can display different colors
 * @param supportsColorTemperature Indicates whether the light supports color temperature adjustment
 * @param brightnessRange The valid brightness range for this light, if supported
 * @param colorTemperatureRange The valid color temperature range for this light, if supported
 */
public record LightCapabilities(
        /** Indicates whether the light can display different colors */
        boolean supportsColor,

        /** Indicates whether the light supports color temperature adjustment */
        boolean supportsColorTemperature,

        /** The valid brightness range for this light, if supported */
        Optional<Range<Integer>> brightnessRange,

        /** The valid color temperature range for this light, if supported */
        Optional<Range<Integer>> colorTemperatureRange) {}
