package space.forloop.hue.model;

/**
 * Represents a range of valid values for a specific light property. Used to define the minimum and
 * maximum values for properties like brightness and color temperature.
 *
 * @param <I> The numeric type of the range (typically Integer)
 * @param min The minimum value in the range
 * @param max The maximum value in the range
 */
public record Range<I extends Number>(I min, I max) {}
