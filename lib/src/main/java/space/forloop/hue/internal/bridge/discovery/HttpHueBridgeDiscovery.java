package space.forloop.hue.internal.bridge.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import retrofit2.Response;
import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.bridge.HueBridgeFactory;
import space.forloop.hue.internal.service.DiscoveryApiService;
import space.forloop.hue.model.BridgeDiscoveryResponse;

@RequiredArgsConstructor
public class HttpHueBridgeDiscovery implements HueBridgeDiscovery {

    private final HueBridgeFactory hueBridgeFactory;

    private final DiscoveryApiService discoveryApiService;

    @Override
    public List<HueBridge> discoverBridges() throws HueDiscoveryException {
        try {
            final Response<List<BridgeDiscoveryResponse>> response =
                    discoveryApiService.discoverBridges().execute();

            if (!response.isSuccessful()) {
                throw new HueDiscoveryException(
                        "HTTP discovery failed. Status code: " + response.code());
            }

            final List<BridgeDiscoveryResponse> bridges = response.body();
            if (bridges == null || bridges.isEmpty()) {
                throw new HueDiscoveryException("No bridges found in discovery response");
            }

            final List<HueBridge> hueBridges = new ArrayList<>(bridges.size());
            for (final BridgeDiscoveryResponse bridge : bridges) {
                hueBridges.add(hueBridgeFactory.create(bridge.internalIpaddress()));
            }

            return hueBridges;

        } catch (final IOException e) {
            throw new HueDiscoveryException(
                    "Failed to discover bridges via HTTP: " + e.getMessage(), e);
        }
    }
}
