package space.forloop.hue.internal;

import java.time.Duration;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import space.forloop.hue.api.AuthenticatedBridge;
import space.forloop.hue.api.HueBridge;
import space.forloop.hue.api.HueClient;
import space.forloop.hue.api.Light;
import space.forloop.hue.internal.bridge.DefaultHueBridge;
import space.forloop.hue.internal.bridge.HueBridgeFactory;
import space.forloop.hue.internal.bridge.authenticated.AuthenticatedBridgeFactory;
import space.forloop.hue.internal.bridge.authenticated.DefaultAuthenticatedBridge;
import space.forloop.hue.internal.bridge.discovery.CompositeHueBridgeDiscovery;
import space.forloop.hue.internal.bridge.discovery.HttpHueBridgeDiscovery;
import space.forloop.hue.internal.bridge.discovery.HueBridgeDiscovery;
import space.forloop.hue.internal.bridge.discovery.MDNSHueBridgeDiscovery;
import space.forloop.hue.internal.client.DefaultHueClient;
import space.forloop.hue.internal.light.DefaultLight;
import space.forloop.hue.internal.light.LightFactory;
import space.forloop.hue.internal.service.DiscoveryApiService;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.internal.service.RetrofitHueApiService;

public class HueModule extends AbstractModule {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final String DISCOVERY_URL = "https://discovery.meethue.com";

    @Override
    protected void configure() {
        install(
                new FactoryModuleBuilder()
                        .implement(HueBridge.class, DefaultHueBridge.class)
                        .build(HueBridgeFactory.class));

        install(
                new FactoryModuleBuilder()
                        .implement(AuthenticatedBridge.class, DefaultAuthenticatedBridge.class)
                        .build(AuthenticatedBridgeFactory.class));

        install(
                new FactoryModuleBuilder()
                        .implement(HueApiService.class, RetrofitHueApiService.class)
                        .build(HueApiServiceFactory.class));

        install(
                new FactoryModuleBuilder()
                        .implement(Light.class, DefaultLight.class)
                        .build(LightFactory.class));

        bind(HueClient.class).to(DefaultHueClient.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .writeTimeout(TIMEOUT)
                .build();
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Provides
    @Singleton
    Retrofit.Builder provideRetrofitBuilder(
            final ObjectMapper objectMapper, final OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(okHttpClient);
    }

    @Provides
    @Singleton
    DiscoveryApiService provideDiscoveryService(final Retrofit.Builder retrofitBuilder) {
        return retrofitBuilder.baseUrl(DISCOVERY_URL).build().create(DiscoveryApiService.class);
    }

    @Provides
    @Singleton
    HueBridgeDiscovery provideBridgeDiscovery(
            final HueBridgeFactory bridgeFactory, final DiscoveryApiService discoveryService) {
        final HueBridgeDiscovery httpDiscovery =
                new HttpHueBridgeDiscovery(bridgeFactory, discoveryService);
        final HueBridgeDiscovery mdnsDiscovery = new MDNSHueBridgeDiscovery(bridgeFactory);
        return new CompositeHueBridgeDiscovery(Arrays.asList(httpDiscovery, mdnsDiscovery));
    }
}
