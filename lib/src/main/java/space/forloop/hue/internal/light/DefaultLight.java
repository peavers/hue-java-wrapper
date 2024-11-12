package space.forloop.hue.internal.light;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.Assisted;

import jakarta.inject.Inject;
import retrofit2.Response;
import space.forloop.hue.api.Light;
import space.forloop.hue.exception.HueException;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.model.LightCapabilities;
import space.forloop.hue.model.LightState;

/**
 * Implementation of the {@link Light} interface representing a Philips Hue light using Retrofit.
 */
public class DefaultLight implements Light {

    private final String id;

    private final HueApiService apiService;

    private final String username;

    private final ObjectMapper objectMapper;

    @Inject
    public DefaultLight(
            @Assisted("id") final String id,
            @Assisted("username") final String username,
            @Assisted("ipAddress") final String ipAddress,
            final HueApiServiceFactory apiServiceFactory,
            final ObjectMapper objectMapper) {
        this.id = id;
        this.apiService = apiServiceFactory.create(ipAddress);
        this.username = username;
        this.objectMapper = objectMapper;
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public LightState getState() throws HueException {
        try {
            final Response<JsonNode> response = apiService.getLight(username, id).execute();

            if (!response.isSuccessful()) {
                throw new HueException("Failed to get light state. Status: " + response.code());
            }

            final JsonNode data = response.body();
            if (data == null) {
                throw new HueException("Light data is null.");
            }

            final JsonNode stateNode = data.get("state");

            return LightState.builder()
                    .on(stateNode.get("on").asBoolean())
                    .brightness(stateNode.has("bri") ? stateNode.get("bri").asInt() : null)
                    .colorTemperature(stateNode.has("ct") ? stateNode.get("ct").asInt() : null)
                    .build();

        } catch (final IOException e) {
            throw new HueException("Failed to get light state: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setState(final LightState state) throws HueException {
        try {
            final JsonNode stateJson = objectMapper.valueToTree(state);

            final Response<List<JsonNode>> response =
                    apiService.setLightState(username, id, stateJson).execute();

            if (!response.isSuccessful()) {
                throw new HueException("Failed to set light state. Status: " + response.code());
            }

        } catch (final IOException e) {
            throw new HueException("Failed to set light state: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public LightCapabilities getCapabilities() throws HueException {
        try {
            final Response<JsonNode> response = apiService.getLight(username, id).execute();

            if (!response.isSuccessful()) {
                throw new HueException(
                        "Failed to get light capabilities. Status: " + response.code());
            }

            final JsonNode data = response.body();
            if (data == null) {
                throw new HueException("Light data is null.");
            }

            final JsonNode capabilitiesNode = data.get("capabilities");

            if (capabilitiesNode != null) {
                return objectMapper.treeToValue(capabilitiesNode, LightCapabilities.class);
            } else {
                return null;
            }

        } catch (final IOException e) {
            throw new HueException("Failed to get light capabilities: " + e.getMessage(), e);
        }
    }
}
