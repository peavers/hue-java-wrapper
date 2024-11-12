package space.forloop.hue.model;

/**
 * Represents authentication credentials for connecting to a Philips Hue Bridge. These credentials
 * are required for making API calls to control Hue devices.
 *
 * @param username The username/identifier assigned by the bridge during authentication
 * @param clientKey The client key used for secure communication with the bridge
 */
public record BridgeAuthentication(
        /** The username/identifier assigned by the bridge during authentication */
        String username,

        /** The client key used for secure communication with the bridge */
        String clientKey) {}
