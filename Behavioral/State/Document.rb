=begin
Advantages of the State Design Pattern:

1. Code Organization:
   - Each state is encapsulated in its own class, making the codebase more modular and easier to maintain.
   - It allows for clear separation of concerns, where each class handles only the behavior associated with a specific state.

2. Eliminates Complex Conditionals:
   - The State pattern replaces large conditional statements (e.g., if/else or case statements) with state-specific classes.
   - This leads to cleaner, more readable code by centralizing state-specific logic within the corresponding classes.

3. Facilitates Extensibility:
   - The pattern adheres to the Open/Closed Principle (OCP) of SOLID, allowing new states and behaviors to be added without modifying existing code.
   - This makes it easier to extend the system with new functionality in a scalable way.

Disadvantages of the State Design Pattern:

1. Increased Complexity:
   - Introducing the State pattern may result in a higher number of classes, which can add unnecessary complexity in simple scenarios.
   - It may not be ideal for systems with a small number of states or straightforward state transitions.

2. Instance Overhead:
   - Creating multiple instances for each state can lead to increased memory usage and processing overhead.
   - Although this overhead is usually minimal, it can become significant in performance-critical applications.

3. Debugging Challenges:
   - Debugging can become more complex because the flow of execution depends on the current state of the object.
   - The logic is distributed across multiple classes, which may make it harder to trace and resolve issues.

=end

class Document
  attr_accessor :state

  def initialize
    @state = Draft.new(self)
  end

  def review
    @state.review
  end

  def publish
    @state.publish
  end
end

class Draft
  def initialize(document)
    @document = document
  end

  def review
    puts "Document under review."
    @document.state = InReview.new(@document)
  end

  def publish
    puts "Document needs to be reviewed before publishing."
  end
end

class InReview
  def initialize(document)
    @document = document
  end

  def review
    puts "Document is already under review."
  end

  def publish
    puts "Document published."
    @document.state = Published.new(@document)
  end
end

class Published
  def initialize(document)
    @document = document
  end

  def review
    puts "Document is already published and cannot be reviewed."
  end

  def publish
    puts "Document is already published."
  end
end

# Case
document = Document.new
document.publish  # "Document needs to be reviewed before publishing."
document.review   # "Document under review."
document.publish  # "Document published."
document.review   # "Document is already published and cannot be reviewed."
