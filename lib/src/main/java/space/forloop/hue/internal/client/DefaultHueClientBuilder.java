package space.forloop.hue.internal.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import okhttp3.OkHttpClient;
import space.forloop.hue.api.HueClient;
import space.forloop.hue.api.HueClientBuilder;
import space.forloop.hue.internal.HueModule;

public class DefaultHueClientBuilder implements HueClientBuilder {

    private ObjectMapper objectMapper;

    private OkHttpClient okHttpClient;

    @Override
    public HueClientBuilder withObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    @Override
    public HueClientBuilder withOkHttpClient(final OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        return this;
    }

    @Override
    public HueClient build() {
        final Module overrideModule =
                binder -> {
                    if (objectMapper != null) {
                        binder.bind(ObjectMapper.class).toInstance(objectMapper);
                    }
                    if (okHttpClient != null) {
                        binder.bind(OkHttpClient.class).toInstance(okHttpClient);
                    }
                };

        final Injector injector =
                Guice.createInjector(Modules.override(new HueModule()).with(overrideModule));

        return injector.getInstance(HueClient.class);
    }
}
