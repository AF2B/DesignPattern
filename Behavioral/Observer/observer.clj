(ns observer
  (:require [clojure.spec.alpha :as s]))

(s/def ::topic keyword?)
(s/def ::message any?)
(s/def ::callback fn?)

(defprotocol Observable
  "Protocol defining Observable behaviors"
  (subscribe [this topic callback] "Subscribes a callback function to a specific topic")
  (unsubscribe [this topic callback] "Removes a callback subscription from a topic")
  (notify [this topic message] "Notifies all subscribers of a topic with a message"))

(defrecord EventBus [subscribers]
  Observable
  (subscribe [this topic callback]
    {:pre [(s/valid? ::topic topic)
           (s/valid? ::callback callback)]}
    (update-in this [:subscribers topic] (fnil conj #{}) callback))
  
  (unsubscribe [this topic callback]
    {:pre [(s/valid? ::topic topic)
           (s/valid? ::callback callback)]}
    (update-in this [:subscribers topic] disj callback))
  
  (notify [this topic message]
    {:pre [(s/valid? ::topic topic)
           (s/valid? ::message message)]}
    (doseq [callback (get-in this [:subscribers topic])]
      (callback message))
    this))

(defn create-event-bus
  "Creates a new event bus instance"
  []
  (->EventBus {}))

(defn create-stateful-subscriber
  "Creates a subscriber that maintains state between notifications"
  [initial-state update-fn]
  (let [state (atom initial-state)]
    (fn [message]
      (swap! state update-fn message))))

(defn create-logging-subscriber
  "Creates a subscriber that logs messages with timestamps"
  [topic-name]
  (fn [message]
    (println (format "[%s][%s] Received: %s"
                     (java.time.LocalDateTime/now)
                     topic-name
                     message))))

(comment
  (def event-bus (create-event-bus))
  
  (def order-logger (create-logging-subscriber "Orders"))
  
  (def bus-with-logger 
    (subscribe event-bus :orders order-logger))
  
  (def order-counter
    (create-stateful-subscriber 0 (fn [state _] (inc state))))
  
  (def bus-with-counter
    (subscribe bus-with-logger :orders order-counter))
  
  (notify bus-with-counter :orders {:id 1 :total 100.0})
  (notify bus-with-counter :orders {:id 2 :total 200.0})
  
  (def final-bus
    (unsubscribe bus-with-counter :orders order-logger))
)

(comment
  (require '[clojure.test :refer [deftest testing is]])
  
  (deftest observer-pattern-test
    (testing "Basic subscription and notification"
      (let [received (atom nil)
            callback #(reset! received %)
            bus (-> (create-event-bus)
                   (subscribe :test callback))]
        (notify bus :test "hello")
        (is (= @received "hello"))))
    
    (testing "Multiple subscribers"
      (let [results (atom [])
            callback-1 #(swap! results conj [:cb1 %])
            callback-2 #(swap! results conj [:cb2 %])
            bus (-> (create-event-bus)
                   (subscribe :test callback-1)
                   (subscribe :test callback-2))]
        (notify bus :test "hello")
        (is (= @results [[:cb1 "hello"] [:cb2 "hello"]]))))
    
    (testing "Unsubscribe"
      (let [received (atom nil)
            callback #(reset! received %)
            bus (-> (create-event-bus)
                   (subscribe :test callback)
                   (unsubscribe :test callback))]
        (notify bus :test "hello")
        (is (nil? @received))))
    
    (testing "Stateful subscriber"
      (let [counter (create-stateful-subscriber 0 (fn [state _] (inc state)))
            bus (subscribe (create-event-bus) :test counter)]
        (notify bus :test "event1")
        (notify bus :test "event2")
        (is (= @(#'observer/state counter) 2))))))