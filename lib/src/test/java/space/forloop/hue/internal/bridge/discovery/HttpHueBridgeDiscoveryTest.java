package space.forloop.hue.internal.bridge.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.bridge.HueBridgeFactory;
import space.forloop.hue.internal.service.DiscoveryApiService;
import space.forloop.hue.model.BridgeDiscoveryResponse;

@ExtendWith(MockitoExtension.class)
class HttpHueBridgeDiscoveryTest {

    private static final String IP_ADDRESS_1 = "192.168.1.100";
    private static final String IP_ADDRESS_2 = "192.168.1.101";

    @Mock private HueBridgeFactory hueBridgeFactory;

    @Mock private DiscoveryApiService discoveryApiService;

    @Mock private Call<List<BridgeDiscoveryResponse>> discoveryCall;

    @Mock private HueBridge hueBridge1;

    @Mock private HueBridge hueBridge2;

    @InjectMocks private HttpHueBridgeDiscovery httpDiscovery;

    @BeforeEach
    void setUp() {
        when(discoveryApiService.discoverBridges()).thenReturn(discoveryCall);
    }

    @Test
    void discoverBridges_successfulResponse_shouldReturnBridges()
            throws IOException, HueDiscoveryException {
        // Arrange
        BridgeDiscoveryResponse bridge1 = new BridgeDiscoveryResponse(null, IP_ADDRESS_1, null);
        BridgeDiscoveryResponse bridge2 = new BridgeDiscoveryResponse(null, IP_ADDRESS_2, null);
        List<BridgeDiscoveryResponse> discoveryResponses = Arrays.asList(bridge1, bridge2);

        when(discoveryCall.execute()).thenReturn(Response.success(discoveryResponses));
        when(hueBridgeFactory.create(IP_ADDRESS_1)).thenReturn(hueBridge1);
        when(hueBridgeFactory.create(IP_ADDRESS_2)).thenReturn(hueBridge2);

        // Act
        List<HueBridge> result = httpDiscovery.discoverBridges();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(hueBridge1));
        assertTrue(result.contains(hueBridge2));
        verify(hueBridgeFactory).create(IP_ADDRESS_1);
        verify(hueBridgeFactory).create(IP_ADDRESS_2);
    }

    @Test
    void discoverBridges_unsuccessfulResponse_shouldThrowException() throws IOException {
        // Arrange
        ResponseBody errorBody =
                ResponseBody.create(
                        MediaType.parse("application/json"), "{\"error\":\"Not Found\"}");
        when(discoveryCall.execute()).thenReturn(Response.error(404, errorBody));

        // Act & Assert
        HueDiscoveryException exception =
                assertThrows(HueDiscoveryException.class, () -> httpDiscovery.discoverBridges());
        assertEquals("HTTP discovery failed. Status code: 404", exception.getMessage());
    }

    @Test
    void discoverBridges_nullResponseBody_shouldThrowException() throws IOException {
        // Arrange
        when(discoveryCall.execute()).thenReturn(Response.success(null));

        // Act & Assert
        HueDiscoveryException exception =
                assertThrows(HueDiscoveryException.class, () -> httpDiscovery.discoverBridges());
        assertEquals("No bridges found in discovery response", exception.getMessage());
    }

    @Test
    void discoverBridges_emptyResponseBody_shouldThrowException() throws IOException {
        // Arrange
        when(discoveryCall.execute()).thenReturn(Response.success(Collections.emptyList()));

        // Act & Assert
        HueDiscoveryException exception =
                assertThrows(HueDiscoveryException.class, () -> httpDiscovery.discoverBridges());
        assertEquals("No bridges found in discovery response", exception.getMessage());
    }

    @Test
    void discoverBridges_ioException_shouldThrowException() throws IOException {
        // Arrange
        IOException ioException = new IOException("Network error");
        when(discoveryCall.execute()).thenThrow(ioException);

        // Act & Assert
        HueDiscoveryException exception =
                assertThrows(HueDiscoveryException.class, () -> httpDiscovery.discoverBridges());
        assertEquals("Failed to discover bridges via HTTP: Network error", exception.getMessage());
        assertSame(ioException, exception.getCause());
    }

    @Test
    void discoverBridges_singleBridge_shouldReturnBridge()
            throws IOException, HueDiscoveryException {
        // Arrange
        BridgeDiscoveryResponse bridge = new BridgeDiscoveryResponse(null, IP_ADDRESS_1, null);
        List<BridgeDiscoveryResponse> discoveryResponses = Collections.singletonList(bridge);

        when(discoveryCall.execute()).thenReturn(Response.success(discoveryResponses));
        when(hueBridgeFactory.create(IP_ADDRESS_1)).thenReturn(hueBridge1);

        // Act
        List<HueBridge> result = httpDiscovery.discoverBridges();

        // Assert
        assertEquals(1, result.size());
        assertSame(hueBridge1, result.get(0));
        verify(hueBridgeFactory).create(IP_ADDRESS_1);
    }

    @Test
    void discoverBridges_bridgeFactoryException_shouldPropagateException() throws IOException {
        // Arrange
        BridgeDiscoveryResponse bridge = new BridgeDiscoveryResponse(null, IP_ADDRESS_1, null);
        List<BridgeDiscoveryResponse> discoveryResponses = Collections.singletonList(bridge);
        RuntimeException factoryException = new RuntimeException("Factory error");

        when(discoveryCall.execute()).thenReturn(Response.success(discoveryResponses));
        when(hueBridgeFactory.create(IP_ADDRESS_1)).thenThrow(factoryException);

        // Act & Assert
        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> httpDiscovery.discoverBridges());
        assertSame(factoryException, thrown);
    }
}
