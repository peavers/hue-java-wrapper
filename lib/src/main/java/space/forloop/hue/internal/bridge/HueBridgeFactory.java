package space.forloop.hue.internal.bridge;

import space.forloop.hue.api.HueBridge;

public interface HueBridgeFactory {
    HueBridge create(String ipAddress);
}
