/*
Real world components:
  - Subject: The products who the customers want to be notified about.
  - Observer: The customer who wants to be notified about the product.
  - ConcreteSubject: The product that the customers want to be notified about.
  - ConcreteObserver: The customer who wants to be notified about the product.
*/

protocol ProductObserver: AnyObject {
    func update(productName: String)
}

class Product {
    private var observers = [ProductObserver]()
    private var inStock: Bool = false
    private var name: String

    init (name: String) {
        self.name = name
    }

    var observersInterested: [ProductObserver] {
        get {
            return observers
        }
    }

    func subscribe(observer: ProductObserver) {
        observers.append(observer)
    }

    func unsubscribe(observer: ProductObserver) {
        if let index = observers.firstIndex(where: { $0 === observer }) {
            observers.remove(at: index)
        }
    }

    func notifyObservers() {
        for observer in observers {
            observer.update(productName: name)
        }
    }

    func setInStock(inStock: Bool) {
        self.inStock = inStock
        if inStock {
            notifyObservers()
        }
    }

    func isInStock() -> Bool {
        return inStock
    }

    func getName() -> String {
        return name
    }
}

class Customer: ProductObserver {
    private var name: String

    init(name: String) {
        self.name = name
    }

    var customerName: String {
        get {
            return name
        }
    }

    func update(productName: String) {
        print("\(name) has been notified that \(productName) is back in stock!")
    }
}

let iPhone14 = Product(name: "iPhone 14")

let customer1 = Customer(name: "Alice")
let customer2 = Customer(name: "Bob")
let customer3 = Customer(name: "Charlie")

iPhone14.subscribe(observer: customer1)
iPhone14.subscribe(observer: customer2)

iPhone14.setInStock(inStock: true)

iPhone14.subscribe(observer: customer3)

iPhone14.setInStock(inStock: true)

for observer in iPhone14.observersInterested {
    if let customer = observer as? Customer {
        print(customer.customerName)
    }
}

iPhone14.unsubscribe(observer: customer2)

for observer in iPhone14.observersInterested {
    if let customer = observer as? Customer {
        print(customer.customerName)
    }
}
