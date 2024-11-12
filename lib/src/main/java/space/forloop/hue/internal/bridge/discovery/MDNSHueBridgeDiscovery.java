package space.forloop.hue.internal.bridge.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import lombok.RequiredArgsConstructor;
import space.forloop.hue.api.HueBridge;
import space.forloop.hue.exception.HueDiscoveryException;
import space.forloop.hue.internal.bridge.HueBridgeFactory;

/** Discovers Hue bridges on the local network using mDNS. */
@RequiredArgsConstructor
public class MDNSHueBridgeDiscovery implements HueBridgeDiscovery {

    private final HueBridgeFactory hueBridgeFactory;

    @Override
    public List<HueBridge> discoverBridges() throws HueDiscoveryException {
        final List<HueBridge> bridges = new ArrayList<>();

        try {
            final InetAddress address = getLocalHostLANAddress();
            try (final JmDNS jmdns = JmDNS.create(address)) {
                final String serviceType = "_hue._tcp.local.";
                final int timeout = 5000;

                final ServiceInfo[] serviceInfos = jmdns.list(serviceType, timeout);

                for (final ServiceInfo serviceInfo : serviceInfos) {
                    final String ipAddress = serviceInfo.getInetAddresses()[0].getHostAddress();
                    bridges.add(hueBridgeFactory.create(ipAddress));
                }

                if (bridges.isEmpty()) {
                    throw new HueDiscoveryException("No Hue bridges found via mDNS.");
                }

                return bridges;
            }
        } catch (final IOException e) {
            throw new HueDiscoveryException(
                    "Failed to discover bridges via mDNS: " + e.getMessage(), e);
        }
    }

    private InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            for (final NetworkInterface networkInterface :
                    Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (final InetAddress inetAddr :
                        Collections.list(networkInterface.getInetAddresses())) {
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            final InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException(
                        "The JDK InetAddress.getLocalHost() method unexpectedly returned null");
            }
            return jdkSuppliedAddress;
        } catch (final Exception e) {
            final UnknownHostException ex =
                    new UnknownHostException("Failed to determine LAN address: " + e);
            ex.initCause(e);
            throw ex;
        }
    }
}
