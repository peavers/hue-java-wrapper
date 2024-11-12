package space.forloop.hue.api;

import space.forloop.hue.exception.HueAuthenticationException;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.model.BridgeAuthentication;

/** Represents a physical Hue bridge and provides operations for interacting with it. */
public interface HueBridge {

    /**
     * Returns the IP of the current bridge
     *
     * @return The bridge's IP address
     */
    String getIpAddress();

    /**
     * Returns the ID of the current bridge
     *
     * @return The bridge's unique identifier
     */
    String getBridgeId() throws HueConnectionException;

    /**
     * Attempts to authenticate with the bridge. The link button must be pressed first.
     *
     * @param applicationName Name to register with the bridge
     * @return Bridge authentication credentials
     * @throws HueAuthenticationException if authentication fails
     */
    BridgeAuthentication authenticate(String applicationName)
            throws HueAuthenticationException, HueConnectionException;

    /**
     * Creates a new authenticated session with existing credentials.
     *
     * @param credentials Previously obtained bridge credentials
     * @return Authenticated bridge session
     */
    AuthenticatedBridge authenticate(BridgeAuthentication credentials);
}
