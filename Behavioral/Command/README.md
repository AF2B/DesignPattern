# Command Design Pattern

[**Versão em Português**](/Behavioral/Command/pt.md) </br>
[**Pусская версия**](/Behavioral/Command/ru.md)

Welcome to the Command Design Pattern section! Here you will find detailed explanations, opinions, and insights on the Command design pattern, including its applications in real-world scenarios. This section is intended for both beginners and experienced professionals looking to enhance their knowledge of design patterns.

## Table of Contents

- [Introduction](#introduction)
- [Command Design Pattern](#command-design-pattern)
  - [Concept](#concept)
- [Applications](#applications)
- [Advantages](#advantages)
- [Contributions](#contributions)

## Introduction

This section is dedicated to the study and understanding of the Command design pattern. Each aspect is explained in detail, with practical examples and use cases. Here, you will find not only implementations but also performance analysis and best practices for utilizing the Command pattern.

## Command Design Pattern

### Concept

The Command design pattern encapsulates a request as a standalone object, so that the code issuing the request (the invoker) no longer needs to know anything about the code that fulfills it (the receiver). Because a command is just an object, it can be stored, queued, passed around, logged, and — since it carries enough information to reverse itself — undone. `command.clj` demonstrates this with a small text-editing example: `InsertTextCommand` and `DeleteTextCommand` each implement the `Command` protocol's `execute`/`undo` pair, a `CommandHistory` (built with `create-invoker`) tracks executed commands on a stack so `undo-last!` can roll them back one at a time, and `MacroCommand` composes several commands into one, executing and undoing them as a single unit.

## Applications

The Command pattern is widely used in various applications such as:

- Undo/redo stacks in editors and other stateful applications
- Task queues and job schedulers, where a command is enqueued now and run later
- Wizards and macro recorders that group several actions into one replayable unit
- Decoupling UI controls (menu items, buttons) from the logic they trigger

## Advantages

- **Decoupling**: The invoker only depends on the `Command` protocol, never on the concrete receiver logic.
- **Composability**: Commands can be combined (see `MacroCommand`) or queued without changing their implementation.
- **Reversibility**: Pairing `execute` with `undo` on the same object keeps the information needed to reverse an action right next to the action itself.

## Contributions

We welcome contributions! If you wish to add new explanations, improvements, or corrections, please follow these steps:

1. Fork this repository.
2. Create a branch for your changes: `git checkout -b feature/new-example`.
3. Open a pull request clearly describing the changes made and the motivation behind them.

Feel free to contribute your knowledge and help enrich this section. Together, we can create a valuable resource for everyone interested in design patterns!

🚀

---
