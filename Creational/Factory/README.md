# Factory Design Pattern

[**Versão em Português**](/Creational/Factory/pt.md) </br>
[**Pусская версия**](/Creational/Factory/ru.md)

Welcome to the Factory Design Pattern section! Here, you will find detailed explanations, examples, and insights into the Factory design pattern, including its applications in real-world scenarios. This section is designed for both beginners and experienced professionals looking to deepen their understanding of design patterns.

## Table of Contents

- [Factory Design Pattern](#factory-design-pattern)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Factory Design Pattern](#factory-design-pattern-1)
    - [Concept](#concept)
    - [Clojure Implementation](#clojure-implementation)
  - [Applications](#applications)
  - [Advantages](#advantages)
  - [Contributions](#contributions)

## Introduction

This section is dedicated to the exploration and understanding of the Factory design pattern. Each aspect is thoroughly explained with practical examples and use cases. You'll find not only implementations but also performance analysis and best practices for applying the Factory pattern effectively.

## Factory Design Pattern

### Concept

The Factory design pattern is a creational pattern that provides an interface for creating objects in a superclass but allows subclasses to alter the type of objects that will be created. It promotes loose coupling between client classes and the classes they instantiate.

This pattern is particularly useful when:
- The exact type of the object cannot be determined until runtime.
- There is a need to centralize object creation logic to promote reuse and consistency.

### Clojure Implementation

Here’s an example of implementing the Factory pattern in Clojure with a logistics theme, allowing the creation of delivery services by air, land, and sea:

```clojure
(ns factory
  (:require [clojure.string :as str]))

(defprotocol DeliveryService
  "Interface for delivery services."
  (calculate-cost [this distance] "Calculates the delivery cost based on the distance.")
  (process-delivery [this package destination] "Executes the delivery of the package to the destination."))

(defrecord AirDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 5 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Air to " destination)))

(defrecord LandDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 2 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Land to " destination)))

(defrecord SeaDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 1 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Sea to " destination)))

(defn delivery-factory
  "Factory that creates a delivery service based on the given type."
  [type]
  (case (str/lower-case type)
    "air"  (->AirDelivery)
    "land" (->LandDelivery)
    "sea"  (->SeaDelivery)
    (throw (IllegalArgumentException.
            (str "Invalid delivery type: " type)))))

(defn calculate-and-deliver
  "Service that uses the factory to calculate the cost and deliver a package."
  [type package destination distance]
  (let [service (delivery-factory type)]
    {:cost (calculate-cost service distance)
     :delivery (process-delivery service package destination)}))

(comment
  (calculate-and-deliver "air" "Box of Parts" "Pernambuco" 100)
  ;; => {:cost 500, :delivery "Package 'Box of Parts' will be delivered via Air to Pernambuco"}

  (calculate-and-deliver "land" "Toolbox" "Rio Grande Do Sul" 50)
  ;; => {:cost 100, :delivery "Package 'Toolbox' will be delivered via Land to Rio Grande Do Sul"}

  (calculate-and-deliver "space" "Heavy Cargo" "Port of Santos" 200)
  ;; => IllegalArgumentException: Invalid delivery type: space
)
```

## Applications

The Factory pattern is widely used in various applications such as:

- **Logistics Systems:** Centralizing the creation of different delivery methods based on dynamic input.
- **UI Components:** Creating buttons, modals, or other components dynamically based on context.
- **Parsing and Serialization:** Creating specific parsers or serializers for different formats (e.g., JSON, XML).
- **Game Development:** Creating characters, weapons, or abilities dynamically based on user actions.

## Advantages

- **Encapsulation of Object Creation:** Separates object creation logic from the main application logic, making the code cleaner and easier to maintain.
- **Scalability:** Adding new types of objects is straightforward and does not require modifying existing code, adhering to the Open/Closed Principle.
- **Consistency:** Ensures uniform initialization of objects across the application.
- **Promotes Loose Coupling:** Clients depend on the factory interface, not concrete implementations.

## Contributions

We welcome contributions! If you wish to add new explanations, improvements, or corrections, please follow these steps:

1. Fork this repository.
2. Create a branch for your changes: `git checkout -b feature/new-example`.
3. Open a pull request clearly describing the changes made and the motivation behind them.

Feel free to share your expertise and help us enrich this section. Together, we can create a valuable resource for everyone interested in design patterns! 🚀

