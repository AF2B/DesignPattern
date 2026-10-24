# Abstract Factory Design Pattern

[**Versão em Português**](/Creational/AbstractFactory/pt.md) </br>
[**Pусская версия**](/Creational/AbstractFactory/ru.md)

Welcome to the Abstract Factory Design Pattern section! Here, you will find detailed explanations, examples, and insights into the Abstract Factory design pattern, including its applications in real-world scenarios. This section is designed for both beginners and experienced professionals looking to deepen their understanding of design patterns.

## Table of Contents

- [Abstract Factory Design Pattern](#abstract-factory-design-pattern)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Abstract Factory Design Pattern](#abstract-factory-design-pattern-1)
    - [Concept](#concept)
    - [Factory vs. Abstract Factory](#factory-vs-abstract-factory)
    - [Java 25 Implementation](#java-25-implementation)
    - [Running](#running)
  - [Applications](#applications)
  - [Advantages](#advantages)
  - [Contributions](#contributions)

## Introduction

This section is dedicated to the exploration and understanding of the Abstract Factory design pattern. It continues the logistics theme of the [Factory](/Creational/Factory/README.md) section, now implemented in **Java 25** with sealed interfaces, records, exhaustive `switch` and record patterns.

## Abstract Factory Design Pattern

### Concept

The Abstract Factory is a creational pattern that provides an interface for creating **families of related objects** without specifying their concrete classes. Each concrete factory produces a complete, consistent family, so client code can never combine products that do not belong together.

This pattern is particularly useful when:
- The system must work with several families of related products.
- Products of one family must be used together, and mixing families is a bug.
- The concrete family is only known at runtime (configuration, user input, environment).

### Factory vs. Abstract Factory

- **Factory** creates **one** product, chosen by a parameter (`delivery-factory "air"` returns an `AirDelivery`).
- **Abstract Factory** creates a **family** of products through one object (`AirLogistics` creates a `CargoPlane`, a `PressurizedCrate` and a `FlightRadar`). Choosing the factory once fixes the whole family.

### Java 25 Implementation

A logistics company ships parcels by air, land and sea. Each mode needs a carrier, a packaging and a tracking channel that only make sense together:

| Family | Carrier | Packaging | Tracker |
|---|---|---|---|
| `AirLogistics` | `CargoPlane` | `PressurizedCrate` (500 kg) | `FlightRadar` |
| `LandLogistics` | `Truck` | `Pallet` (1,000 kg) | `GpsTracker` |
| `SeaLogistics` | `CargoShip` | `ShippingContainer` (28,000 kg) | `AisTransponder` |

Key excerpts (full source in [`AbstractFactory.java`](/Creational/AbstractFactory/AbstractFactory.java)):

```java
sealed interface LogisticsFactory permits AirLogistics, LandLogistics, SeaLogistics {
    Carrier createCarrier();
    Packaging createPackaging();
    Tracker createTracker();
}

record AirLogistics() implements LogisticsFactory {
    public Carrier createCarrier() { return new CargoPlane(); }
    public Packaging createPackaging() { return new PressurizedCrate(); }
    public Tracker createTracker() { return new FlightRadar(); }
}

// Exhaustive: adding a TransportMode without a factory is a compile error.
static LogisticsFactory factoryFor(TransportMode mode) {
    return switch (mode) {
        case AIR -> new AirLogistics();
        case LAND -> new LandLogistics();
        case SEA -> new SeaLogistics();
    };
}

// The client only sees abstract products.
static ShipmentResult ship(
        LogisticsFactory factory,
        Parcel parcel,
        String destination,
        int distanceKm) {
    var carrier = factory.createCarrier();
    var packaging = factory.createPackaging();
    var tracker = factory.createTracker();

    if (parcel.weightKg() > packaging.maxWeightKg()) {
        return new Overweight(
                packaging.name(),
                parcel.weightKg(),
                packaging.maxWeightKg());
    }

    var cost = carrier.costPerKm().multiply(BigDecimal.valueOf(distanceKm));

    return new Scheduled(
            carrier.name(),
            packaging.name(),
            cost,
            tracker.trackingChannel(destination));
}

// Expected failures are values (sealed result), rendered with record patterns.
static String render(ShipmentResult result) {
    return switch (result) {
        case Scheduled(var carrier, var packaging, var cost, var tracking) -> "...";
        case Overweight(var packaging, var weightKg, var maxWeightKg) -> "...";
    };
}
```

Design notes:
- **Sealed interfaces** close each product hierarchy and the factory hierarchy, so every `switch` over them is checked for exhaustiveness by the compiler.
- **Expected failures are values**: an overweight parcel returns `Overweight`, and an unknown mode returns `Optional.empty()` from `TransportMode.fromLabel`. No exceptions for business rules.
- **Money uses `BigDecimal`**, never `double`.

### Running

The file is a Java 25 compact source file (JEP 512), runnable without a build tool:

```bash
java Creational/AbstractFactory/AbstractFactory.java
```

Output:

```text
Scheduled: Cargo Plane + Pressurized Crate, cost 500.00, tracked by Flight radar to Pernambuco
Scheduled: Truck + Pallet, cost 100.00, tracked by GPS on the road to Rio Grande Do Sul
Rejected: 2000 kg exceeds the Pressurized Crate limit of 500 kg
Scheduled: Cargo Ship + Shipping Container, cost 200.00, tracked by AIS transponder to Port of Santos
Unknown transport mode: space
```

## Applications

The Abstract Factory pattern is widely used in various applications such as:

- **Logistics Systems:** Building consistent carrier/packaging/tracking bundles per transport mode.
- **UI Toolkits:** Creating buttons, menus and dialogs that share one look and feel (e.g., light vs. dark theme, or one widget set per operating system).
- **Persistence Layers:** Producing connections, commands and transactions for one database vendor at a time.
- **Cloud Providers:** Creating storage, queue and compute clients for a single provider behind common interfaces.
- **Testing:** Swapping a whole family of real infrastructure for in-memory fakes with a single factory.

## Advantages

- **Consistency Between Products:** A family is created by one factory, so incompatible combinations cannot happen.
- **Isolation of Concrete Classes:** Clients depend only on abstract products and the abstract factory.
- **Easy Family Swapping:** Changing the factory instance changes the entire family at once.
- **Open/Closed for Families:** Adding a new family means adding a new factory and its products, without touching client code.
- **Trade-off:** Adding a new *kind* of product (e.g., `Insurance`) changes the factory interface and every concrete factory. The sealed hierarchy makes the compiler point out every place that must change.

## Contributions

We welcome contributions! If you wish to add new explanations, improvements, or corrections, please follow these steps:

1. Fork this repository.
2. Create a branch for your changes: `git checkout -b feature/new-example`.
3. Open a pull request clearly describing the changes made and the motivation behind them.

Feel free to share your expertise and help us enrich this section. Together, we can create a valuable resource for everyone interested in design patterns! 🚀
