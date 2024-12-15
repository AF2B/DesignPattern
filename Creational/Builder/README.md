# Builder Design Pattern in Clojure

[**Versão em Português**](#versao-em-portugues)  
[**English Version**](#introduction)

Welcome to the **Builder Design Pattern** section! Here, you will find detailed explanations, examples, and insights into the Builder design pattern, including its practical applications in real-world scenarios. This section is designed for both beginners and experienced professionals looking to deepen their understanding of design patterns, particularly in the context of functional programming with Clojure.

---

## Table of Contents

- [Builder Design Pattern in Clojure](#builder-design-pattern-in-clojure)
  - [Table of Contents](#table-of-contents)
  - [Introduction](#introduction)
  - [Builder Design Pattern](#builder-design-pattern)
    - [Concept](#concept)
  - [Applications](#applications)
    - [Example: Financial Report Builder](#example-financial-report-builder)

---

## Introduction

The **Builder Design Pattern** is a creational pattern that provides a way to construct complex objects step by step. Unlike other creational patterns, Builder focuses on the construction process itself, allowing for incremental construction and customization. In this implementation, we adapt the pattern to Clojure, leveraging its functional nature and imutability to build domain-specific objects in a clean and composable manner.

This repository provides:

- A clean implementation of the Builder pattern in Clojure.  
- Practical examples, including the construction of a customizable financial report system.   

---

## Builder Design Pattern

### Concept

The **Builder Pattern** separates the construction of a complex object from its representation, allowing the same construction process to create different representations. It is particularly useful when:

- The construction process involves multiple steps or configurations.  
- The resulting object is immutable and requires intermediate states.  
- Customization of the construction process is needed for different scenarios.  

In Clojure, we achieve this by composing functions that incrementally build the desired structure, using immutable maps and declarative transformations.

---

## Applications

The Builder pattern is widely used in scenarios such as:

- **Report Generation**: Customizing reports with different fields like titles, authors, dates, and content.  
- **Configuration Objects**: Creating objects with many optional parameters while maintaining immutability.  
- **UI Elements**: Building reusable, configurable components in a step-by-step manner.  
- **Data Pipelines**: Constructing multi-step data processing configurations.

### Example: Financial Report Builder

The provided implementation demonstrates how to construct a financial report using a functional Builder:

```clojure
(ns builder)

(defn create-report
  "Creates an initial structure for the report."
  []
  {:title    nil
   :author   nil
   :date     nil
   :content  []
   :summary  nil})

(defn set-title
  "Sets the title of the report."
  [report title]
  (assoc report :title title))

(defn set-author
  "Sets the author of the report."
  [report author]
  (assoc report :author author))

(defn set-date
  "Sets the date of the report."
  [report date]
  (assoc report :date date))

(defn add-content
  "Adds a section to the report content."
  [report section]
  (update report :content conj section))

(defn set-summary
  "Adds a summary to the report."
  [report summary]
  (assoc report :summary summary))

(defn build-report
  "Validates and returns the finalized report."
  [report]
  (if (and (:title report) (:author report) (:content report))
    report
    (throw (IllegalArgumentException. "Invalid report: title, author, and content are mandatory."))))

(defn generate-sample-report []
  (-> (create-report)
      (set-title "Annual Financial Report")
      (set-author "Finance Department")
      (set-date "2024-12-13")
      (add-content "Introduction to financial results.")
      (add-content "Analysis of expenses and revenues.")
      (set-summary "This report provides a detailed overview of the annual financial performance.")
      (build-report)))

(comment
  (generate-sample-report)
  ;; => {:title "Annual Financial Report", 
  ;;     :author "Finance Department", 
  ;;     :date "2024-12-13", 
  ;;     :content ["Introduction to financial results." "Analysis of expenses and revenues."], 
  ;;     :summary "This report provides a detailed overview of the annual financial performance."}
)
```