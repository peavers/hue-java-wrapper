package space.forloop.hue.internal.bridge.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.bridge.HueBridgeFactory;

@ExtendWith(MockitoExtension.class)
class MDNSHueBridgeDiscoveryTest {

    @Mock private HueBridgeFactory hueBridgeFactory;

    @Mock private NetworkInterface networkInterface;

    @Mock private JmDNS jmDNS;

    @Mock private HueBridge hueBridge1;

    @Mock private HueBridge hueBridge2;

    @Mock private ServiceInfo serviceInfo1;

    @Mock private ServiceInfo serviceInfo2;

    private MDNSHueBridgeDiscovery mdnsDiscovery;

    @BeforeEach
    void setUp() {
        mdnsDiscovery = new MDNSHueBridgeDiscovery(hueBridgeFactory);
    }

    @Test
    void discoverBridges_successfulDiscovery_shouldReturnBridges() throws Exception {
        // Arrange
        InetAddress localAddress = mock(InetAddress.class);
        InetAddress bridgeAddress1 = mock(InetAddress.class);
        InetAddress bridgeAddress2 = mock(InetAddress.class);

        when(localAddress.isSiteLocalAddress()).thenReturn(true);
        when(localAddress.isLoopbackAddress()).thenReturn(false);
        when(bridgeAddress1.getHostAddress()).thenReturn("192.168.1.10");
        when(bridgeAddress2.getHostAddress()).thenReturn("192.168.1.11");

        try (MockedStatic<NetworkInterface> networkInterfaceMock =
                        mockStatic(NetworkInterface.class);
                MockedStatic<JmDNS> jmDNSMock = mockStatic(JmDNS.class)) {

            // Mock network interface enumeration
            Enumeration<NetworkInterface> interfaceEnumeration =
                    Collections.enumeration(Collections.singletonList(networkInterface));
            networkInterfaceMock
                    .when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(interfaceEnumeration);

            // Mock interface addresses
            Enumeration<InetAddress> addressEnumeration =
                    Collections.enumeration(Collections.singletonList(localAddress));
            when(networkInterface.getInetAddresses()).thenReturn(addressEnumeration);

            // Mock JmDNS behavior
            jmDNSMock.when(() -> JmDNS.create(localAddress)).thenReturn(jmDNS);
            ServiceInfo[] serviceInfos = {serviceInfo1, serviceInfo2};
            when(jmDNS.list("_hue._tcp.local.", 5000)).thenReturn(serviceInfos);

            // Mock ServiceInfo behavior
            when(serviceInfo1.getInetAddresses()).thenReturn(new InetAddress[] {bridgeAddress1});
            when(serviceInfo2.getInetAddresses()).thenReturn(new InetAddress[] {bridgeAddress2});

            // Mock bridge factory
            when(hueBridgeFactory.create("192.168.1.10")).thenReturn(hueBridge1);
            when(hueBridgeFactory.create("192.168.1.11")).thenReturn(hueBridge2);

            // Act
            var result = mdnsDiscovery.discoverBridges();

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.contains(hueBridge1));
            assertTrue(result.contains(hueBridge2));
            verify(hueBridgeFactory).create("192.168.1.10");
            verify(hueBridgeFactory).create("192.168.1.11");
            verify(jmDNS).close();
        }
    }

    @Test
    void discoverBridges_noBridgesFound_shouldThrowException() throws Exception {
        // Arrange
        InetAddress localAddress = mock(InetAddress.class);
        when(localAddress.isSiteLocalAddress()).thenReturn(true);
        when(localAddress.isLoopbackAddress()).thenReturn(false);

        try (MockedStatic<NetworkInterface> networkInterfaceMock =
                        mockStatic(NetworkInterface.class);
                MockedStatic<JmDNS> jmDNSMock = mockStatic(JmDNS.class)) {

            // Mock network interface behavior
            Enumeration<NetworkInterface> interfaceEnumeration =
                    Collections.enumeration(Collections.singletonList(networkInterface));
            networkInterfaceMock
                    .when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(interfaceEnumeration);

            // Mock interface addresses
            Enumeration<InetAddress> addressEnumeration =
                    Collections.enumeration(Collections.singletonList(localAddress));
            when(networkInterface.getInetAddresses()).thenReturn(addressEnumeration);

            // Mock JmDNS behavior
            jmDNSMock.when(() -> JmDNS.create(localAddress)).thenReturn(jmDNS);
            when(jmDNS.list("_hue._tcp.local.", 5000)).thenReturn(new ServiceInfo[0]);

            // Act & Assert
            HueDiscoveryException exception =
                    assertThrows(
                            HueDiscoveryException.class, () -> mdnsDiscovery.discoverBridges());
            assertEquals("No Hue bridges found via mDNS.", exception.getMessage());
            verify(jmDNS).close();
        }
    }

    @Test
    void discoverBridges_jmdnsError_shouldThrowException() {
        // Arrange
        InetAddress localAddress = mock(InetAddress.class);

        try (MockedStatic<NetworkInterface> networkInterfaceMock =
                        mockStatic(NetworkInterface.class);
                MockedStatic<JmDNS> jmDNSMock = mockStatic(JmDNS.class)) {

            // Mock network interface behavior
            Enumeration<NetworkInterface> interfaceEnumeration =
                    Collections.enumeration(Collections.singletonList(networkInterface));
            networkInterfaceMock
                    .when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(interfaceEnumeration);

            // Mock interface addresses
            Enumeration<InetAddress> addressEnumeration =
                    Collections.enumeration(Collections.singletonList(localAddress));
            when(networkInterface.getInetAddresses()).thenReturn(addressEnumeration);
            when(localAddress.isLoopbackAddress()).thenReturn(false);
            when(localAddress.isSiteLocalAddress()).thenReturn(true);

            // Mock JmDNS error
            IOException jmdnsError = new IOException("JmDNS error");
            jmDNSMock.when(() -> JmDNS.create(localAddress)).thenThrow(jmdnsError);

            // Act & Assert
            HueDiscoveryException exception =
                    assertThrows(
                            HueDiscoveryException.class, () -> mdnsDiscovery.discoverBridges());
            assertEquals(
                    "Failed to discover bridges via mDNS: JmDNS error", exception.getMessage());
            assertSame(jmdnsError, exception.getCause());
        }
    }

    @Test
    void discoverBridges_noValidNetworkInterface_shouldFallbackToLocalhost() throws Exception {
        // Arrange
        InetAddress localhost = mock(InetAddress.class);
        InetAddress bridgeAddress = mock(InetAddress.class);
        when(bridgeAddress.getHostAddress()).thenReturn("192.168.1.10");

        try (MockedStatic<NetworkInterface> networkInterfaceMock =
                        mockStatic(NetworkInterface.class);
                MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class);
                MockedStatic<JmDNS> jmDNSMock = mockStatic(JmDNS.class)) {

            // Mock empty network interfaces
            networkInterfaceMock
                    .when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(Collections.enumeration(Collections.emptyList()));

            // Mock localhost fallback
            inetAddressMock.when(InetAddress::getLocalHost).thenReturn(localhost);

            // Mock JmDNS behavior
            when(JmDNS.create(localhost)).thenReturn(jmDNS);
            ServiceInfo[] serviceInfos = {serviceInfo1};
            when(jmDNS.list("_hue._tcp.local.", 5000)).thenReturn(serviceInfos);

            // Mock ServiceInfo
            when(serviceInfo1.getInetAddresses()).thenReturn(new InetAddress[] {bridgeAddress});
            when(hueBridgeFactory.create("192.168.1.10")).thenReturn(hueBridge1);

            // Act
            var result = mdnsDiscovery.discoverBridges();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(hueBridge1));
            verify(jmDNS).close();
        }
    }

    @Test
    void discoverBridges_networkInterfaceError_shouldThrowException() {
        try (MockedStatic<NetworkInterface> networkInterfaceMock =
                mockStatic(NetworkInterface.class)) {
            // Mock network interface error with SocketException
            networkInterfaceMock
                    .when(NetworkInterface::getNetworkInterfaces)
                    .thenThrow(new java.net.SocketException("Network interface error"));

            // Act & Assert
            HueDiscoveryException exception =
                    assertThrows(
                            HueDiscoveryException.class, () -> mdnsDiscovery.discoverBridges());
            assertTrue(exception.getMessage().contains("Failed to determine LAN address"));
        }
    }
}
