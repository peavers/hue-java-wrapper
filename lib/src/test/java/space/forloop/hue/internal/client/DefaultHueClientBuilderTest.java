package space.forloop.hue.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import space.forloop.hue.api.HueClient;

@ExtendWith(MockitoExtension.class)
class DefaultHueClientBuilderTest {

    @Test
    void build_withDefaultConfiguration_shouldCreateClient() {
        // Arrange
        DefaultHueClientBuilder builder = new DefaultHueClientBuilder();

        // Act
        HueClient client = builder.build();

        // Assert
        assertNotNull(client);
        assertTrue(client instanceof DefaultHueClient);
    }

    @Test
    void build_withCustomObjectMapper_shouldCreateClientWithCustomMapper() {
        // Arrange
        DefaultHueClientBuilder builder = new DefaultHueClientBuilder();
        ObjectMapper customMapper = new ObjectMapper();

        // Act
        HueClient client = builder.withObjectMapper(customMapper).build();

        // Assert
        assertNotNull(client);
        assertTrue(client instanceof DefaultHueClient);
    }

    @Test
    void build_withCustomHttpClient_shouldCreateClientWithCustomHttpClient() {
        // Arrange
        DefaultHueClientBuilder builder = new DefaultHueClientBuilder();
        OkHttpClient customClient =
                new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(30)).build();

        // Act
        HueClient client = builder.withOkHttpClient(customClient).build();

        // Assert
        assertNotNull(client);
        assertTrue(client instanceof DefaultHueClient);
    }

    @Test
    void build_withAllCustomConfigurations_shouldCreateConfiguredClient() {
        // Arrange
        DefaultHueClientBuilder builder = new DefaultHueClientBuilder();
        ObjectMapper customMapper = new ObjectMapper();
        OkHttpClient customClient =
                new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(30)).build();

        // Act
        HueClient client =
                builder.withObjectMapper(customMapper).withOkHttpClient(customClient).build();

        // Assert
        assertNotNull(client);
        assertTrue(client instanceof DefaultHueClient);
    }

    @Test
    void withObjectMapper_shouldReturnSameBuilder() {
        // Arrange
        DefaultHueClientBuilder builder = new DefaultHueClientBuilder();
        ObjectMapper customMapper = new ObjectMapper();

        // Act
        DefaultHueClientBuilder result =
                (DefaultHueClientBuilder) builder.withObjectMapper(customMapper);

        // Assert
        assertSame(builder, result);
    }

    @Test
    void withOkHttpClient_shouldReturnSameBuilder() {
        // Arrange
        DefaultHueClientBuilder builder = new DefaultHueClientBuilder();
        OkHttpClient customClient = new OkHttpClient();

        // Act
        DefaultHueClientBuilder result =
                (DefaultHueClientBuilder) builder.withOkHttpClient(customClient);

        // Assert
        assertSame(builder, result);
    }
}
