package space.forloop.hue.internal.client;

import java.util.List;

import com.google.inject.Inject;

import space.forloop.hue.api.HueBridge;
import space.forloop.hue.api.HueClient;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.bridge.HueBridgeFactory;
import space.forloop.hue.internal.bridge.discovery.HueBridgeDiscovery;

public class DefaultHueClient implements HueClient {

    private final HueBridgeFactory bridgeFactory;

    private final HueBridgeDiscovery bridgeDiscovery;

    @Inject
    public DefaultHueClient(
            final HueBridgeDiscovery bridgeDiscovery, final HueBridgeFactory bridgeFactory) {
        this.bridgeDiscovery = bridgeDiscovery;
        this.bridgeFactory = bridgeFactory;
    }

    @Override
    public List<HueBridge> discoverBridges() throws HueDiscoveryException, HueConnectionException {
        return bridgeDiscovery.discoverBridges();
    }

    @Override
    public HueBridge connectToBridge(final String ipAddress) {
        return bridgeFactory.create(ipAddress);
    }
}
