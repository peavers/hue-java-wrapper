package space.forloop.hue.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response received when discovering Philips Hue bridges on the local network. This
 * information is used to locate and connect to Hue bridges.
 *
 * @param id Unique identifier of the Hue bridge
 * @param internalIpaddress The local IP address of the bridge on the network
 * @param port The port number the bridge is listening on
 */
public record BridgeDiscoveryResponse(
        /** Unique identifier of the Hue bridge */
        String id,

        /** The local IP address of the bridge on the network */
        @JsonProperty("internalipaddress") String internalIpaddress,

        /** The port number the bridge is listening on */
        String port) {}
