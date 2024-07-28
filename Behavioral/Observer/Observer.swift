/*
Observer pattern are an Behavioral pattern where an object are created with more than one object dependents on it.
When the object changes its state, all the dependents are notified and updated automatically with no need to call them.

This pattern is used when we have a one-to-many relationship between objects such as if one object is modified, its dependents are to be notified automatically.

Participants:
- Subject: The subject participant is the object that has the state to be observed by the observers.
  - maintain a list of observers.
  - provides methods to attach and detach observers.
  - notifies observers when the state changes.
- Observer: The observer participant is the object that is interested in the state of the subject.
  - defines an interface for updating the observer.
- ConcreteSubject: Concrete Subject class
  - Extends the Subject class and keep the interest of the observers.
  - Notifies the observers when the state changes.
- ConcreteObserver: Concrete Observer class
  - Implements the Observer interface to keep the state consistent with the ConcreteSubject.

        +-----------+
        | Subject   |
        +-----------+
        | +attach() |
        | +detach() |
        | +notify() |
        +-----------+
              |
      ----------------
      |              |
+-----------+   +-----------+
| Concrete  |   |  Observer |
|  Subject  |   +-----------+
+-----------+   | +update() |
| -state    |   +-----------+
| +getState()|
| +setState()|
+-----------+
*/

protocol Observer: AnyObject {
    func update(message: String)
}

class Subject {
    private var observers = [Observer]()
    private var state: String = ""

    func attach(observer: Observer) {
        observers.append(observer)
    }

    func detach(observer: Observer) {
        if let index = observers.firstIndex(where: { $0 === observer }) {
            observers.remove(at: index)
        }
    }

    func notifyObservers() {
        for observer in observers {
            observer.update(message: state)
        }
    }

    func setState(state: String) {
        self.state = state
        notifyObservers()
    }

    func getState() -> String {
        return state
    }
}

class ConcreteObserver: Observer {
    private var name: String

    init(name: String) {
        self.name = name
    }

    func update(message: String) {
        print("\(name) received message: \(message)")
    }
}

let subject = Subject()

let observer1 = ConcreteObserver(name: "Observer 1")
let observer2 = ConcreteObserver(name: "Observer 2")

subject.attach(observer: observer1)
subject.attach(observer: observer2)

subject.setState(state: "State changed!")
