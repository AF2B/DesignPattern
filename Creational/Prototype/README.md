# Prototype Design Pattern

[**Versão em Português**](/Creational/Prototype/pt.md) </br>
[**Pусская версия**](/Creational/Prototype/ru.md)

Welcome to the Prototype Design Pattern section! Here, you will find detailed explanations, examples, and insights into the Prototype design pattern, including its applications in real-world scenarios. This section is designed for both beginners and experienced professionals looking to deepen their understanding of design patterns.

## Table of Contents

- [Prototype Design Pattern](#prototype-design-pattern)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Prototype Design Pattern](#prototype-design-pattern-1)
    - [Concept](#concept)
    - [Shallow vs. Deep Copy](#shallow-vs-deep-copy)
    - [Why Not `Cloneable`?](#why-not-cloneable)
    - [Java 25 Implementation](#java-25-implementation)
    - [Running](#running)
  - [Applications](#applications)
  - [Advantages](#advantages)
  - [Contributions](#contributions)

## Introduction

This section is dedicated to the exploration and understanding of the Prototype design pattern. It follows the logistics theme of the other creational patterns, implemented in **Java 25** with a sealed class hierarchy, copy constructors, flexible constructor bodies and exhaustive pattern matching.

## Prototype Design Pattern

### Concept

The Prototype is a creational pattern that creates new objects by **copying an existing, preconfigured instance** (the prototype) instead of building them from scratch. The client asks the prototype to copy itself, so it never needs to know the concrete class being copied.

This pattern is particularly useful when:
- Creating an object is expensive or requires many configuration steps.
- The system needs many objects that differ only slightly from a few templates.
- The concrete class to instantiate is only known at runtime, and the client must stay decoupled from it.

### Shallow vs. Deep Copy

A **shallow** copy shares mutable fields (such as a `List`) with the original: changing the copy's list also changes the prototype. A **deep** copy duplicates those fields, so each copy is fully independent. This implementation always deep-copies mutable state, and the registry stores private copies of the prototypes it receives.

### Why Not `Cloneable`?

Java's `Cloneable`/`Object.clone()` is a broken mechanism: `Cloneable` has no `clone()` method, `clone()` creates objects without calling a constructor, it is shallow by default and it conflicts with `final` fields. *Effective Java* (3rd ed., Item 13) recommends **copy constructors** or copy factories instead, which is what `copy()` uses here.

### Java 25 Implementation

A logistics company keeps preconfigured shipment orders. New orders are copies of a template that the clerk only customizes with the recipient and destination:

| Template | Type | Service | Handling instructions | Customs |
|---|---|---|---|---|
| `EXPRESS_DOMESTIC` | `DomesticOrder` | Express | Signature required | — |
| `FRAGILE_DOMESTIC` | `DomesticOrder` | Standard | Handle with care, Keep upright | — |
| `INTERNATIONAL_ELECTRONICS` | `InternationalOrder` | International Priority | Handle with care, Contains lithium batteries | Laptop |

Key excerpts (full source in [`Prototype.java`](/Creational/Prototype/Prototype.java)):

```java
abstract static sealed class ShipmentOrder permits DomesticOrder, InternationalOrder {

    private final String service;
    private final List<String> handlingInstructions;
    private String recipient = UNASSIGNED;
    private String destination = UNASSIGNED;

    // Copy constructor: deep-copies mutable fields.
    protected ShipmentOrder(ShipmentOrder source) {
        this.service = source.service;
        this.handlingInstructions = new ArrayList<>(source.handlingInstructions);
        this.recipient = source.recipient;
        this.destination = source.destination;
    }

    abstract ShipmentOrder copy();
}

static final class InternationalOrder extends ShipmentOrder {

    private final List<String> customsItems;

    private InternationalOrder(InternationalOrder source) {
        this.customsItems = new ArrayList<>(source.customsItems); // before super() (JEP 513)
        super(source);
    }

    @Override
    InternationalOrder copy() { // covariant return type
        return new InternationalOrder(this);
    }
}

static final class PrototypeRegistry {

    private final Map<TemplateKind, ShipmentOrder> prototypes;

    // The client gets a copy without knowing the concrete class.
    ShipmentOrder spawn(TemplateKind kind) {
        return prototypes.get(kind).copy();
    }
}
```

Design notes:
- **`static` nested classes**: in a compact source file every top-level declaration becomes a member of an implicit class. Records, enums and interfaces are implicitly static, but classes are not, so they are declared `static` to avoid capturing an implicit outer instance.
- **Sealed hierarchy**: `render` switches over `ShipmentOrder` exhaustively; a new order type without a rendering is a compile error.
- **Flexible constructor bodies** (JEP 513, final in Java 25) let `InternationalOrder` initialize its own fields *before* `super(...)`, so the object is never observed half-initialized.
- **The registry is total**: its constructor rejects a map that misses any `TemplateKind` (an invariant violation), so `spawn` never returns `null`. Unknown user input is handled as `Optional.empty()` by `TemplateKind.fromLabel`.

### Running

The file is a Java 25 compact source file (JEP 512), runnable without a build tool:

```bash
java Creational/Prototype/Prototype.java
```

Output (note that the second spawn of `INTERNATIONAL_ELECTRONICS` is untouched by the changes made to the first one):

```text
[domestic] Standard to Ana at Recife [Handle with care, Keep upright]
[domestic] Standard to Bruno at Porto Alegre [Handle with care, Keep upright, Signature required]
[international] International Priority to Carla at Lisbon [Handle with care, Contains lithium batteries] customs=[Laptop, Smartphone]
[international] International Priority to <unassigned> at <unassigned> [Handle with care, Contains lithium batteries] customs=[Laptop]
Unknown template: same-day-drone
```

## Applications

The Prototype pattern is widely used in various applications such as:

- **Logistics and E-commerce:** Creating orders, labels and quotes from preconfigured templates.
- **Document Editors:** Duplicating shapes, slides or pages with all their formatting.
- **Game Development:** Spawning enemies, items or particles from preconfigured archetypes.
- **Configuration:** Deriving environment-specific configurations from a base configuration.
- **Testing:** Building test fixtures by copying a valid base object and changing only what the test needs.

## Advantages

- **Decoupling from Concrete Classes:** Clients copy objects through `copy()` without knowing their type.
- **Cheaper Creation:** Expensive setup is done once in the prototype and reused by every copy.
- **Fewer Subclasses and Constructors:** Variations are expressed as registered prototypes instead of new classes.
- **Runtime Flexibility:** Prototypes can be added, replaced or configured while the application runs.
- **Trade-off:** Every class in the hierarchy must implement copying correctly, and deep copies of object graphs with cycles need extra care.

## Contributions

We welcome contributions! If you wish to add new explanations, improvements, or corrections, please follow these steps:

1. Fork this repository.
2. Create a branch for your changes: `git checkout -b feature/new-example`.
3. Open a pull request clearly describing the changes made and the motivation behind them.

Feel free to share your expertise and help us enrich this section. Together, we can create a valuable resource for everyone interested in design patterns! 🚀
