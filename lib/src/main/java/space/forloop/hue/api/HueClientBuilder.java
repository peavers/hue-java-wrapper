package space.forloop.hue.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;

/** Builder interface for constructing a {@link HueClient} instance with customizable components. */
public interface HueClientBuilder {

    /**
     * Sets the {@link ObjectMapper} to be used by the {@code HueClient}.
     *
     * @param objectMapper the object mapper for JSON serialization and deserialization
     * @return the current instance of {@code HueClientBuilder} for method chaining
     */
    HueClientBuilder withObjectMapper(ObjectMapper objectMapper);

    /**
     * Sets the {@link OkHttpClient} to be used by the {@code HueClient}.
     *
     * @param okHttpClient the HTTP client to use for making requests
     * @return the current instance of {@code HueClientBuilder} for method chaining
     */
    HueClientBuilder withOkHttpClient(OkHttpClient okHttpClient);

    /**
     * Builds and returns a new instance of {@link HueClient} with the configured components.
     *
     * @return a fully configured {@code HueClient} instance
     */
    HueClient build();
}
