# ADR-002: In-Memory Messaging (No Kafka/Redpanda)

**Status**: Accepted  
**Date**: 2026-02-02  
**Decision Makers**: Project Owner  

## Context

The original architecture specification (Phase 01) called for Kafka-based messaging using Redpanda as the message broker. This would provide:
- Distributed event streaming
- Message persistence and replay
- Decoupled microservices communication
- Scalability across multiple instances

However, for the current phase of development:
1. All services run as a monolith or single-instance deployments
2. The complexity of managing Kafka/Redpanda adds operational overhead
3. Local development requires additional Docker containers (Redpanda + Console)
4. The event-driven patterns can be preserved with in-memory channels

## Decision

**We will use Quarkus in-memory messaging channels instead of Kafka/Redpanda.**

SmallRye Reactive Messaging supports internal channels that work without any external connector:
- When no `connector` is configured for a channel, it becomes an in-memory channel
- Messages are passed directly between producers and consumers within the same JVM
- The `@Incoming` and `@Outgoing` annotations remain unchanged
- The code is **Kafka-ready** - switching to Kafka requires only configuration changes

## Consequences

### Positive
- Simpler architecture with fewer moving parts
- Faster local development (no Redpanda containers needed)
- Reduced resource consumption (no JVM for Kafka client, no broker)
- Faster startup times
- Same reactive programming model preserved
- Easy migration path to Kafka when needed

### Negative
- No message persistence (messages lost on restart)
- No distributed messaging (single JVM only)
- No message replay capability
- No consumer groups or partitioning
- Cannot scale horizontally with shared message queues

### Neutral
- Event-driven architecture patterns remain intact
- Code changes are minimal (comments only)
- Can migrate to Kafka by changing configuration

## Implementation

### Dependencies Changed

**Before (Kafka)**:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-messaging-kafka</artifactId>
</dependency>
```

**After (In-Memory)**:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-messaging</artifactId>
</dependency>
```

### Configuration Changed

**Before (Kafka)**:
```properties
kafka.bootstrap.servers=localhost:19092
mp.messaging.outgoing.orders-out.connector=smallrye-kafka
mp.messaging.outgoing.orders-out.topic=orders
mp.messaging.incoming.orders-in.connector=smallrye-kafka
mp.messaging.incoming.orders-in.topic=orders
```

**After (In-Memory)**:
```properties
# No connector = in-memory channel
mp.messaging.outgoing.orders-out.merge=true
mp.messaging.incoming.orders-in.connector=smallrye-in-memory
```

### Docker Compose Changed

Redpanda and Redpanda Console services commented out in `docker/docker-compose.yml`.

## Migration Path to Kafka

When distributed messaging is needed:

1. **Add Kafka dependency** to `pom.xml`:
   ```xml
   <dependency>
       <groupId>io.quarkus</groupId>
       <artifactId>quarkus-messaging-kafka</artifactId>
   </dependency>
   ```

2. **Activate Kafka profile**:
   ```bash
   mvn quarkus:dev -Dquarkus.profile=kafka
   ```

3. **Uncomment Kafka configuration** in `application.properties`:
   ```properties
   %kafka.kafka.bootstrap.servers=localhost:19092
   %kafka.mp.messaging.outgoing.orders-out.connector=smallrye-kafka
   # ... etc
   ```

4. **Uncomment Redpanda services** in `docker-compose.yml`

## References

- [Quarkus Reactive Messaging Guide](https://quarkus.io/guides/messaging)
- [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging/)
- ADR-001: Simple JWT Authentication
- Original spec: `specs/phase-01-core-infrastructure.md`

