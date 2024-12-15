# Singleton Design Pattern

[**Versão em Português**](/Creational/Singleton/pt.md) </br>
[**Pусская версия**](/Creational/Singleton/ru.md)

Welcome to the Singleton Design Pattern section! Here, you will find detailed explanations, examples, and insights into the Singleton design pattern, including its applications in real-world scenarios. This section is designed for both beginners and experienced professionals looking to deepen their understanding of design patterns.

## Table of Contents

- [Singleton Design Pattern](#singleton-design-pattern)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Singleton Design Pattern](#singleton-design-pattern-1)
    - [Concept](#concept)
  - [Applications](#applications)
    - [Example in Clojure](#example-in-clojure)
  - [Advantages](#advantages)
  - [Contributions](#contributions)

## Introduction

This section is dedicated to the exploration and understanding of the Singleton design pattern. Each aspect is thoroughly explained with practical examples and use cases. You'll find not only implementations but also performance analysis and best practices for applying the Singleton pattern effectively.

## Singleton Design Pattern

### Concept

The Singleton design pattern ensures that a class has only one instance and provides a global point of access to it. It’s widely used in scenarios where controlling resource usage or maintaining a consistent state across the application is critical. The Singleton is particularly useful when implementing services such as logging, caching, or configuration management.

In this repository, we demonstrate how to implement the Singleton pattern in **Clojure** using a Logger as the central use case.

## Applications

The Singleton pattern is widely used in various applications, such as:

- Logging services, where a single logger instance manages all logging operations.
- Configuration management, ensuring consistent access to global application settings.
- Resource management, such as connection pools or thread-safe caching.
- File systems, maintaining a single access point to a file or directory manager.

### Example in Clojure

The Logger example in this repository demonstrates a Singleton that logs messages to a file. The implementation ensures that only one logger instance exists across the application. Check out the source code:
```clojure
(ns singleton)

(defprotocol LoggerService
  "Protocol defining logging operations."
  (log-info [this message] "Logs an informational message.")
  (log-error [this message] "Logs an error message.")
  (log-debug [this message] "Logs a debug message."))

(defrecord FileLogger [log-file]
  LoggerService
  (log-info [_ message]
    (spit log-file (str (java.time.Instant/now) " [INFO]: " message "\n") :append true))
  (log-error [_ message]
    (spit log-file (str (java.time.Instant/now) " [ERROR]: " message "\n") :append true))
  (log-debug [_ message]
    (spit log-file (str (java.time.Instant/now) " [DEBUG]: " message "\n") :append true)))

(defonce logger-instance
  (atom nil))

(defn get-logger
  "Returns the singleton instance of the LoggerService."
  []
  (if-let [instance @logger-instance]
    instance
    (let [new-instance (->FileLogger "application.log")]
      (reset! logger-instance new-instance)
      new-instance)))

(defn log-endpoint
  "A Pedestal handler that logs requests and responses."
  [request]
  (let [logger (get-logger)]
    (log-info logger (str "Received request: " (:uri request)))
    {:status 200
     :body   "Request logged successfully!"}))

(require '[io.pedestal.http :as http])

(def service
  {:env                  :prod
   ::http/routes         #{["/log" :get log-endpoint]}
   ::http/type           :jetty
   ::http/port           8080})

(comment
  ;; Start the server
  (http/create-server service)
  ;; curl http://localhost:8080/log
)
```

## Advantages

- **Resource Optimization**: Ensures only one instance of a resource-intensive object is created, saving memory and processing power.
- **Global Access Point**: Provides a controlled and consistent way to access the single instance.
- **Thread Safety**: Guarantees safe access to the instance in concurrent applications.
- **Adheres to Clean Code and SOLID**: The implementation promotes maintainability and extensibility.

## Contributions

We welcome contributions! If you wish to add new explanations, improvements, or corrections, please follow these steps:

1. Fork this repository.
2. Create a branch for your changes: `git checkout -b feature/new-example`.
3. Open a pull request clearly describing the changes made and the motivation behind them.

Feel free to share your expertise and help us enrich this section. Together, we can create a valuable resource for everyone interested in design patterns!

🚀

