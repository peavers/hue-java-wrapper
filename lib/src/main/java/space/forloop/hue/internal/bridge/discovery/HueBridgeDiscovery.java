package space.forloop.hue.internal.bridge.discovery;

import java.util.List;

import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.exception.HueDiscoveryException;

public interface HueBridgeDiscovery {
    /**
     * Discovers Hue bridges using a specific strategy.
     *
     * @return a list of discovered Hue bridges
     * @throws HueDiscoveryException if discovery fails
     * @throws HueConnectionException if connecting to a discovered bridge fails
     */
    List<HueBridge> discoverBridges() throws HueDiscoveryException, HueConnectionException;
}
