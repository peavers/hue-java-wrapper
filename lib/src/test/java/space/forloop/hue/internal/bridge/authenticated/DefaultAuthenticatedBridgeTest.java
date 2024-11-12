package space.forloop.hue.internal.bridge.authenticated;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import space.forloop.hue.api.Light;
import space.forloop.hue.exception.HueException;
import space.forloop.hue.internal.light.LightFactory;
import space.forloop.hue.internal.service.HueApiService;
import space.forloop.hue.internal.service.HueApiServiceFactory;
import space.forloop.hue.model.BridgeAuthentication;

@ExtendWith(MockitoExtension.class)
class DefaultAuthenticatedBridgeTest {

    private static final String IP_ADDRESS = "192.168.1.100";
    private static final String USERNAME = "testUsername";
    private static final String LIGHT_ID_1 = "1";
    private static final String LIGHT_ID_2 = "2";

    @Mock private HueApiServiceFactory apiServiceFactory;

    @Mock private HueApiService apiService;

    @Mock private LightFactory lightFactory;

    @Mock private Light light1;

    @Mock private Light light2;

    @Mock private Call<Map<String, JsonNode>> lightsCall;

    @Mock private JsonNode lightNode1;

    @Mock private JsonNode lightNode2;

    private DefaultAuthenticatedBridge authenticatedBridge;
    private BridgeAuthentication credentials;

    @BeforeEach
    void setUp() {
        credentials = new BridgeAuthentication(USERNAME, null);
        when(apiServiceFactory.create(IP_ADDRESS)).thenReturn(apiService);
        authenticatedBridge =
                new DefaultAuthenticatedBridge(
                        apiServiceFactory, IP_ADDRESS, credentials, lightFactory);
    }

    @Test
    void getLights_successfulResponse_shouldReturnLights() throws Exception {
        // Arrange
        Map<String, JsonNode> lightsMap = new HashMap<>();
        lightsMap.put(LIGHT_ID_1, lightNode1);
        lightsMap.put(LIGHT_ID_2, lightNode2);

        when(apiService.getLights(USERNAME)).thenReturn(lightsCall);
        when(lightsCall.execute()).thenReturn(Response.success(lightsMap));
        when(lightFactory.create(LIGHT_ID_1, USERNAME, IP_ADDRESS)).thenReturn(light1);
        when(lightFactory.create(LIGHT_ID_2, USERNAME, IP_ADDRESS)).thenReturn(light2);

        // Act
        Collection<Light> result = authenticatedBridge.getLights();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(light1));
        assertTrue(result.contains(light2));
        verify(lightFactory).create(LIGHT_ID_1, USERNAME, IP_ADDRESS);
        verify(lightFactory).create(LIGHT_ID_2, USERNAME, IP_ADDRESS);
    }

    @Test
    void getLights_unsuccessfulResponse_shouldThrowException() throws Exception {
        // Arrange
        ResponseBody errorBody =
                ResponseBody.create(
                        MediaType.parse("application/json"), "{\"error\":\"Not Found\"}");
        when(apiService.getLights(USERNAME)).thenReturn(lightsCall);
        when(lightsCall.execute()).thenReturn(Response.error(404, errorBody));

        // Act & Assert
        HueException exception =
                assertThrows(HueException.class, () -> authenticatedBridge.getLights());
        assertEquals("Failed to get lights. Status: 404", exception.getMessage());
        verify(lightFactory, never()).create(any(), any(), any());
    }

    @Test
    void getLights_nullResponseBody_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLights(USERNAME)).thenReturn(lightsCall);
        when(lightsCall.execute()).thenReturn(Response.success(null));

        // Act & Assert
        HueException exception =
                assertThrows(HueException.class, () -> authenticatedBridge.getLights());
        assertEquals("No lights found in the response.", exception.getMessage());
        verify(lightFactory, never()).create(any(), any(), any());
    }

    @Test
    void getLights_emptyResponseBody_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLights(USERNAME)).thenReturn(lightsCall);
        when(lightsCall.execute()).thenReturn(Response.success(new HashMap<>()));

        // Act & Assert
        HueException exception =
                assertThrows(HueException.class, () -> authenticatedBridge.getLights());
        assertEquals("No lights found in the response.", exception.getMessage());
        verify(lightFactory, never()).create(any(), any(), any());
    }

    @Test
    void getLights_ioException_shouldThrowException() throws Exception {
        // Arrange
        when(apiService.getLights(USERNAME)).thenReturn(lightsCall);
        IOException ioException = new IOException("Network error");
        when(lightsCall.execute()).thenThrow(ioException);

        // Act & Assert
        HueException exception =
                assertThrows(HueException.class, () -> authenticatedBridge.getLights());
        assertEquals("Failed to get lights: Network error", exception.getMessage());
        assertSame(ioException, exception.getCause());
        verify(lightFactory, never()).create(any(), any(), any());
    }

    @Test
    void getLights_lightFactoryError_shouldPropagateException() throws Exception {
        // Arrange
        Map<String, JsonNode> lightsMap = new HashMap<>();
        lightsMap.put(LIGHT_ID_1, lightNode1);

        RuntimeException factoryException = new RuntimeException("Factory error");
        when(apiService.getLights(USERNAME)).thenReturn(lightsCall);
        when(lightsCall.execute()).thenReturn(Response.success(lightsMap));
        when(lightFactory.create(LIGHT_ID_1, USERNAME, IP_ADDRESS)).thenThrow(factoryException);

        // Act & Assert
        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> authenticatedBridge.getLights());
        assertSame(factoryException, thrown);
    }
}
