# Java Philips Hue Client Library

A modern, intuitive Java library for controlling Philips Hue smart lighting systems. Built with dependency injection,
clean architecture principles, and comprehensive error handling to provide a robust and maintainable solution.

Currently only supports basic features authentication and manipulating light state. Will add to this as requirements for
downstream projects evolve, or people want something.

## Installation

The library is available through GitHub Packages. You'll need to configure your build tool with GitHub authentication to
access the package.

### Maven

1. Add the GitHub Packages repository to your `settings.xml` file (usually found in `~/.m2/settings.xml`):

```xml

<settings>
    <servers>
        <server>
            <id>github</id>
            <username>${env.USERNAME}</username>
            <password>${env.TOKEN}</password>
        </server>
    </servers>
</settings>
```

2. Add the repository and dependency to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/peavers/hue-java-wrapper</url>
    </repository>
</repositories>

<dependency>
<groupId>space.forloop.hue</groupId>
<artifactId>hue-java-wrapper</artifactId>
<version>$latestVersion</version>
</dependency>
```

### Gradle

1. Add the GitHub Packages repository and dependency to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    mavenLocal()

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/peavers/hue-java-wrapper")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'space.forloop.hue:hue-java-wrapper:$latestVersion'
}
```

2. Set your GitHub credentials either through environment variables (`USERNAME` and `TOKEN`) or in your
   `gradle.properties` file:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Note: Your GitHub token needs the `read:packages` scope to download packages.

## Quick Start

Here's a simple example to get you started:

```java
class Example {
    public static void main(String[] args) {
        // Create a client
        HueClient client = HueClient.builder().build();

        // Discover bridges
        List<HueBridge> bridges = client.discoverBridges();
        HueBridge bridge = bridges.get(0);

        // Authenticate (press the link button on the bridge first)
        BridgeAuthentication auth = bridge.authenticate("MyApp");

        // Get an authenticated bridge instance
        AuthenticatedBridge authBridge = bridge.authenticate(auth);

        // Control lights
        Collection<Light> lights = authBridge.getLights();
        for (Light light : lights) {
            LightState state = LightState.builder()
                    .on(true)
                    .brightness(254)
                    .colorTemperature(300)
                    .build();
            light.setState(state);
        }
    }
}
```

## Architecture

### Key Components

1. **HueClient**: The main entry point for the library. Handles bridge discovery and connection.
2. **HueBridge**: Represents a physical Hue bridge, providing authentication and configuration.
3. **AuthenticatedBridge**: Provides access to lights and other bridge features after authentication.
4. **Light**: Represents individual light bulbs, allowing state control and capability querying.

### Design Choices

#### Dependency Injection with Guice

Google Guice for dependency injection to:

- Promote loose coupling between components
- Facilitate easier testing through dependency substitution
- Provide a clean, modular architecture

#### Retrofit for Network Operations

Retrofit was selected for HTTP communication because it:

- Provides a type-safe HTTP client
- Offers excellent async support
- Has built-in JSON serialization
- Is widely used and well-maintained

#### Builder Pattern

The builder pattern is used for client configuration to:

- Allow optional customization of HTTP client and object mapper
- Maintain immutability
- Provide a fluent, readable API

#### Custom Exceptions

We implemented a hierarchy of custom exceptions to:

- Provide clear error handling paths
- Enable specific error handling for different failure modes
- Maintain type safety in error handling

## Advanced Usage

### Custom Configuration

```java
class Example {
    public static void main(String[] args) {
        // Use your own object mapper
        ObjectMapper customMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        // Own version of OkHttpClient
        OkHttpClient customClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Use the builder to wire them in
        HueClient client = HueClient.builder()
                .withObjectMapper(customMapper)
                .withOkHttpClient(customClient)
                .build();
    }
}
```

### Error Handling

```java
class Example {
    public static void main(String[] args) {
        try {
            List<HueBridge> bridges = client.discoverBridges();
        } catch (
                HueDiscoveryException e) {
            // Handle discovery failure
        } catch (
                HueConnectionException e) {
            // Handle connection failure
        }

        try {
            BridgeAuthentication auth = bridge.authenticate("MyApp");
        } catch (
                HueAuthenticationException e) {
            // Handle authentication failure
            System.err.println("Please press the link button on the bridge and try again");
        }
    }
}
```

## Best Practices

1. **Store Authentication**: Save the `BridgeAuthentication` after initial setup to avoid requiring link button presses
   on subsequent connections.

2. **Error Handling**: Always handle specific exceptions (`HueDiscoveryException`, `HueConnectionException`,
   `HueAuthenticationException`) to provide appropriate user feedback.

3**Resource Management**: The client uses OkHttp which manages its own resources. However, consider implementing a
shutdown method if needed in your application.

## Thread Safety

The library is designed to be thread-safe. All mutable state is properly encapsulated, and the underlying HTTP client (
OkHttp) is thread-safe by design.

## Testing

The library is designed with testing in mind:

- Interfaces are used throughout to allow mocking
- Dependency injection makes it easy to substitute test implementations
- Factory patterns enable easy creation of test objects

## Requirements

- Java 17 or higher
- Philips Hue Bridge (v2 or later)

---
