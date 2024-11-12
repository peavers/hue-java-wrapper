package space.forloop.hue.internal.bridge.discovery;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.exception.HueDiscoveryException;

/** Tries multiple discovery strategies in order until bridges are found. */
@RequiredArgsConstructor
public class CompositeHueBridgeDiscovery implements HueBridgeDiscovery {

    private final List<HueBridgeDiscovery> discoveryStrategies;

    @Override
    public List<HueBridge> discoverBridges() throws HueDiscoveryException, HueConnectionException {
        List<HueBridge> bridges;
        final List<Exception> exceptions = new ArrayList<>();

        for (final HueBridgeDiscovery strategy : discoveryStrategies) {
            try {
                bridges = strategy.discoverBridges();
                if (!bridges.isEmpty()) {
                    return bridges;
                }
            } catch (final HueDiscoveryException | HueConnectionException e) {
                exceptions.add(e);
            }
        }

        // If no bridges found and exceptions occurred, throw the last exception
        if (!exceptions.isEmpty()) {
            final Exception lastException = exceptions.getLast();

            if (lastException instanceof HueDiscoveryException) {
                throw (HueDiscoveryException) lastException;
            } else {
                throw (HueConnectionException) lastException;
            }
        }

        throw new HueDiscoveryException("No Hue bridges found using any discovery strategy.");
    }
}
