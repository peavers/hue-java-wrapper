package space.forloop.hue.internal.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import retrofit2.Call;
import retrofit2.Retrofit;

public class RetrofitHueApiService implements HueApiService {
    private final HueApiService delegate;

    @Inject
    public RetrofitHueApiService(
            @Assisted final String ipAddress, final Retrofit.Builder retrofitBuilder) {
        this.delegate =
                retrofitBuilder
                        .baseUrl("http://" + ipAddress + "/")
                        .build()
                        .create(HueApiService.class);
    }

    @Override
    public Call<JsonNode> validateConnection() {
        return delegate.validateConnection();
    }

    @Override
    public Call<JsonNode> getConfig() {
        return delegate.getConfig();
    }

    @Override
    public Call<List<JsonNode>> authenticate(final JsonNode body) {
        return delegate.authenticate(body);
    }

    @Override
    public Call<Map<String, JsonNode>> getLights(final String username) {
        return delegate.getLights(username);
    }

    @Override
    public Call<List<JsonNode>> setLightState(
            final String username, final String lightId, final JsonNode body) {
        return delegate.setLightState(username, lightId, body);
    }

    @Override
    public Call<JsonNode> getLight(final String username, final String lightId) {
        return delegate.getLight(username, lightId);
    }
}
