package space.forloop.hue.internal.bridge.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueConnectionException;
import space.forloop.hue.exception.HueDiscoveryException;

@ExtendWith(MockitoExtension.class)
class CompositeHueBridgeDiscoveryTest {

    @Mock private HueBridgeDiscovery strategy1;

    @Mock private HueBridgeDiscovery strategy2;

    @Mock private HueBridge bridge1;

    @Mock private HueBridge bridge2;

    private CompositeHueBridgeDiscovery compositeDiscovery;

    @BeforeEach
    void setUp() {
        compositeDiscovery = new CompositeHueBridgeDiscovery(Arrays.asList(strategy1, strategy2));
    }

    @Test
    void discoverBridges_firstStrategySucceeds_shouldReturnBridges() throws Exception {
        // Arrange
        List<HueBridge> expectedBridges = Arrays.asList(bridge1, bridge2);
        when(strategy1.discoverBridges()).thenReturn(expectedBridges);

        // Act
        List<HueBridge> result = compositeDiscovery.discoverBridges();

        // Assert
        assertSame(expectedBridges, result);
        verify(strategy1).discoverBridges();
        verify(strategy2, never()).discoverBridges();
    }

    @Test
    void discoverBridges_firstStrategyEmpty_secondStrategySucceeds_shouldReturnBridges()
            throws Exception {
        // Arrange
        List<HueBridge> expectedBridges = Arrays.asList(bridge1, bridge2);
        when(strategy1.discoverBridges()).thenReturn(Collections.emptyList());
        when(strategy2.discoverBridges()).thenReturn(expectedBridges);

        // Act
        List<HueBridge> result = compositeDiscovery.discoverBridges();

        // Assert
        assertSame(expectedBridges, result);
        verify(strategy1).discoverBridges();
        verify(strategy2).discoverBridges();
    }

    @Test
    void discoverBridges_allStrategiesEmpty_shouldThrowException() throws Exception {
        // Arrange
        when(strategy1.discoverBridges()).thenReturn(Collections.emptyList());
        when(strategy2.discoverBridges()).thenReturn(Collections.emptyList());

        // Act & Assert
        HueDiscoveryException exception =
                assertThrows(
                        HueDiscoveryException.class, () -> compositeDiscovery.discoverBridges());
        assertEquals("No Hue bridges found using any discovery strategy.", exception.getMessage());
        verify(strategy1).discoverBridges();
        verify(strategy2).discoverBridges();
    }

    @Test
    void discoverBridges_firstStrategyFails_secondStrategySucceeds_shouldReturnBridges()
            throws Exception {
        // Arrange
        List<HueBridge> expectedBridges = Arrays.asList(bridge1, bridge2);
        when(strategy1.discoverBridges()).thenThrow(new HueDiscoveryException("Strategy 1 failed"));
        when(strategy2.discoverBridges()).thenReturn(expectedBridges);

        // Act
        List<HueBridge> result = compositeDiscovery.discoverBridges();

        // Assert
        assertSame(expectedBridges, result);
        verify(strategy1).discoverBridges();
        verify(strategy2).discoverBridges();
    }

    @Test
    void discoverBridges_allStrategiesFail_shouldThrowLastHueDiscoveryException() throws Exception {
        // Arrange
        HueDiscoveryException firstException = new HueDiscoveryException("Strategy 1 failed");
        HueDiscoveryException secondException = new HueDiscoveryException("Strategy 2 failed");

        when(strategy1.discoverBridges()).thenThrow(firstException);
        when(strategy2.discoverBridges()).thenThrow(secondException);

        // Act & Assert
        HueDiscoveryException thrown =
                assertThrows(
                        HueDiscoveryException.class, () -> compositeDiscovery.discoverBridges());

        assertSame(secondException, thrown);
        verify(strategy1).discoverBridges();
        verify(strategy2).discoverBridges();
    }

    @Test
    void discoverBridges_allStrategiesFail_shouldThrowLastHueConnectionException()
            throws Exception {
        // Arrange
        HueConnectionException firstException = new HueConnectionException("Strategy 1 failed");
        HueConnectionException secondException = new HueConnectionException("Strategy 2 failed");

        when(strategy1.discoverBridges()).thenThrow(firstException);
        when(strategy2.discoverBridges()).thenThrow(secondException);

        // Act & Assert
        HueConnectionException thrown =
                assertThrows(
                        HueConnectionException.class, () -> compositeDiscovery.discoverBridges());

        assertSame(secondException, thrown);
        verify(strategy1).discoverBridges();
        verify(strategy2).discoverBridges();
    }

    @Test
    void discoverBridges_mixedExceptions_shouldThrowLastException() throws Exception {
        // Arrange
        HueDiscoveryException firstException = new HueDiscoveryException("Strategy 1 failed");
        HueConnectionException secondException = new HueConnectionException("Strategy 2 failed");

        when(strategy1.discoverBridges()).thenThrow(firstException);
        when(strategy2.discoverBridges()).thenThrow(secondException);

        // Act & Assert
        HueConnectionException thrown =
                assertThrows(
                        HueConnectionException.class, () -> compositeDiscovery.discoverBridges());

        assertSame(secondException, thrown);
        verify(strategy1).discoverBridges();
        verify(strategy2).discoverBridges();
    }

    @Test
    void discoverBridges_emptyStrategyList_shouldThrowException() {
        // Arrange
        compositeDiscovery = new CompositeHueBridgeDiscovery(new ArrayList<>());

        // Act & Assert
        HueDiscoveryException exception =
                assertThrows(
                        HueDiscoveryException.class, () -> compositeDiscovery.discoverBridges());
        assertEquals("No Hue bridges found using any discovery strategy.", exception.getMessage());
    }
}
