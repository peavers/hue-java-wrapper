package space.forloop.hue.internal.bridge.authenticated;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import retrofit2.Response;
import space.forloop.hue.api.AuthenticatedBridge;
import space.forloop.hue.api.Light;
import space.forloop.hue.exception.HueException;
import space.forloop.hue.internal.light.LightFactory;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.model.BridgeAuthentication;

public class DefaultAuthenticatedBridge implements AuthenticatedBridge {

    private final HueApiService apiService;

    private final BridgeAuthentication credentials;

    private final LightFactory lightFactory;

    private final String ipAddress;

    @Inject
    public DefaultAuthenticatedBridge(
            final HueApiServiceFactory apiServiceFactory,
            @Assisted final String ipAddress,
            @Assisted final BridgeAuthentication credentials,
            final LightFactory lightFactory) {
        this.lightFactory = lightFactory;
        this.ipAddress = ipAddress;
        this.apiService = apiServiceFactory.create(ipAddress);
        this.credentials = credentials;
    }

    @Override
    public Collection<Light> getLights() throws HueException {
        try {
            final Response<Map<String, JsonNode>> response =
                    apiService.getLights(credentials.username()).execute();

            if (!response.isSuccessful()) {
                throw new HueException("Failed to get lights. Status: " + response.code());
            }

            final Map<String, JsonNode> lightsMap = response.body();

            if (lightsMap == null || lightsMap.isEmpty()) {
                throw new HueException("No lights found in the response.");
            }

            return lightsMap.keySet().stream()
                    .map(id -> lightFactory.create(id, credentials.username(), ipAddress))
                    .collect(Collectors.toList());

        } catch (final IOException e) {
            throw new HueException("Failed to get lights: " + e.getMessage(), e);
        }
    }
}
