package space.forloop.hue.internal.bridge.authenticated;

import space.forloop.hue.api.AuthenticatedBridge;
import space.forloop.hue.model.BridgeAuthentication;

public interface AuthenticatedBridgeFactory {
    AuthenticatedBridge create(String ipAddress, BridgeAuthentication credentials);
}
