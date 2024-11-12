package space.forloop.hue.api;

import java.util.List;

import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.client.DefaultHueClientBuilder;

/**
 * main.java.org.example.Main interface for interacting with Philips Hue bridges and devices.
 * Provides high-level operations for bridge discovery, authentication, and device management.
 */
public interface HueClient {
    /**
     * Construct a builder for the Hue client.
     *
     * @return DefaultHueClientBuilder
     */
    static HueClientBuilder builder() {
        return new DefaultHueClientBuilder();
    }

    /**
     * Discovers all Hue bridges on the local network using UPnP.
     *
     * @return List of discovered bridges
     * @throws HueDiscoveryException if bridge discovery fails
     */
    List<HueBridge> discoverBridges() throws HueDiscoveryException, HueConnectionException;

    /**
     * Connects to a specific Hue bridge by IP address.
     *
     * @param ipAddress The IP address of the Hue bridge
     * @return Connected bridge instance
     * @throws HueConnectionException if connection fails
     */
    HueBridge connectToBridge(String ipAddress) throws HueConnectionException;
}
