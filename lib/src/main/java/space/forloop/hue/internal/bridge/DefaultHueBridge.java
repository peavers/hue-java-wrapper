package space.forloop.hue.internal.bridge;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import retrofit2.Response;
import space.forloop.hue.api.AuthenticatedBridge;
import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueAuthenticationException;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.internal.bridge.authenticated.AuthenticatedBridgeFactory;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.model.BridgeAuthentication;

public class DefaultHueBridge implements HueBridge {

    private final String ipAddress;

    private final HueApiService hueApiService;

    private final ObjectMapper objectMapper;

    private final AuthenticatedBridgeFactory authenticatedBridgeFactory;

    @Inject
    public DefaultHueBridge(
            @Assisted final String ipAddress,
            final HueApiServiceFactory apiServiceFactory,
            final ObjectMapper objectMapper,
            final AuthenticatedBridgeFactory authenticatedBridgeFactory) {
        this.ipAddress = ipAddress;
        this.hueApiService = apiServiceFactory.create(ipAddress);
        this.objectMapper = objectMapper;
        this.authenticatedBridgeFactory = authenticatedBridgeFactory;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String getBridgeId() throws HueConnectionException {
        try {
            final Response<JsonNode> response = hueApiService.getConfig().execute();

            if (response.isSuccessful() && response.body() != null) {
                final JsonNode config = response.body();
                final String bridgeId = config.path("bridgeid").asText();

                if (bridgeId.isEmpty()) {
                    throw new HueConnectionException("Bridge ID not found in the response.");
                }

                return bridgeId;
            } else {
                throw new HueConnectionException(
                        "Failed to get bridge ID. Status code: " + response.code());
            }
        } catch (final IOException e) {
            throw new HueConnectionException("Failed to get bridge ID: " + e.getMessage(), e);
        }
    }

    @Override
    public BridgeAuthentication authenticate(final String applicationName)
            throws HueAuthenticationException {
        try {
            final ObjectNode requestBody =
                    objectMapper.createObjectNode().put("devicetype", applicationName);

            final Response<List<JsonNode>> response =
                    hueApiService.authenticate(requestBody).execute();

            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                final JsonNode responseNode = response.body().getFirst();

                if (responseNode.has("error")) {
                    final String errorDescription =
                            responseNode.get("error").get("description").asText();
                    throw new HueAuthenticationException(
                            "Authentication failed: " + errorDescription);
                }

                final String username = responseNode.get("success").get("username").asText();
                return new BridgeAuthentication(username, null);
            } else {
                throw new HueAuthenticationException(
                        "Authentication failed: unexpected response from bridge");
            }
        } catch (final IOException e) {
            throw new HueAuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthenticatedBridge authenticate(final BridgeAuthentication credentials) {
        return authenticatedBridgeFactory.create(ipAddress, credentials);
    }
}
