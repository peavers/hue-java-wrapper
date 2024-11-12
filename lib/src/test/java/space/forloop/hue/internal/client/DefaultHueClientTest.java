package space.forloop.hue.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.bridge.HueBridgeFactory;
import space.forloop.hue.internal.bridge.discovery.HueBridgeDiscovery;

@ExtendWith(MockitoExtension.class)
class DefaultHueClientTest {

    private static final String IP_ADDRESS = "192.168.1.100";

    @Mock private HueBridgeFactory bridgeFactory;

    @Mock private HueBridgeDiscovery bridgeDiscovery;

    @Mock private HueBridge bridge1;

    @Mock private HueBridge bridge2;

    @InjectMocks private DefaultHueClient hueClient;

    @Test
    void discoverBridges_successfulDiscovery_shouldReturnBridges() throws Exception {
        // Arrange
        List<HueBridge> expectedBridges = Arrays.asList(bridge1, bridge2);
        when(bridgeDiscovery.discoverBridges()).thenReturn(expectedBridges);

        // Act
        List<HueBridge> result = hueClient.discoverBridges();

        // Assert
        assertSame(expectedBridges, result);
        verify(bridgeDiscovery).discoverBridges();
    }

    @Test
    void discoverBridges_discoveryException_shouldPropagateException() throws Exception {
        // Arrange
        HueDiscoveryException discoveryException = new HueDiscoveryException("Discovery failed");
        when(bridgeDiscovery.discoverBridges()).thenThrow(discoveryException);

        // Act & Assert
        HueDiscoveryException thrown =
                assertThrows(HueDiscoveryException.class, () -> hueClient.discoverBridges());
        assertSame(discoveryException, thrown);
    }

    @Test
    void discoverBridges_connectionException_shouldPropagateException() throws Exception {
        // Arrange
        HueConnectionException connectionException =
                new HueConnectionException("Connection failed");
        when(bridgeDiscovery.discoverBridges()).thenThrow(connectionException);

        // Act & Assert
        HueConnectionException thrown =
                assertThrows(HueConnectionException.class, () -> hueClient.discoverBridges());
        assertSame(connectionException, thrown);
    }

    @Test
    void connectToBridge_shouldReturnBridge() {
        // Arrange
        when(bridgeFactory.create(IP_ADDRESS)).thenReturn(bridge1);

        // Act
        HueBridge result = hueClient.connectToBridge(IP_ADDRESS);

        // Assert
        assertSame(bridge1, result);
        verify(bridgeFactory).create(IP_ADDRESS);
    }
}
