(ns atomrefs
  (:require [clojure.pprint :as pp])
  (:gen-class))

;; === Atoms Example: Game Score System ===
(def game-state 
  (atom {:player1 {:score 0 :lives 3}
         :player2 {:score 0 :lives 3}}))

(defn update-score 
  "Updates a player's score atomically"
  [player points]
  (swap! game-state update-in [player :score] + points))

(defn lose-life 
  "Decrements a player's life atomically"
  [player]
  (swap! game-state update-in [player :lives] dec))

(defn reset-game 
  "Resets the game state"
  []
  (reset! game-state {:player1 {:score 0 :lives 3}
                      :player2 {:score 0 :lives 3}}))

;; === Refs Example: Banking System ===
(def checking-account (ref 1000))
(def savings-account (ref 5000))

(defn transfer
  "Transfers money between accounts within a transaction"
  [from to amount]
  (dosync
    (when (>= @from amount)
      (alter from - amount)
      (alter to + amount)
      true)))

(defn deposit
  "Deposits money into an account"
  [account amount]
  (dosync
    (alter account + amount)))

(defn withdraw
  "Withdraws money from an account if sufficient funds"
  [account amount]
  (dosync
    (when (>= @account amount)
      (alter account - amount)
      true)))

;; === Demonstrating Coordination ===
(def inventory (ref {:apples 10 :oranges 5}))
(def shopping-cart (ref {}))

(defn add-to-cart
  "Adds an item to cart if available in inventory"
  [item quantity]
  (dosync
    (when (>= (get @inventory item 0) quantity)
      (alter inventory update item - quantity)
      (alter shopping-cart update item (fnil + 0) quantity)
      true)))

(defn return-item
  "Returns an item from cart to inventory"
  [item quantity]
  (dosync
    (when (>= (get @shopping-cart item 0) quantity)
      (alter shopping-cart update item - quantity)
      (alter inventory update item (fnil + 0) quantity)
      true)))

;; === Real-world Scenarios ===

;; 1. Atom for Cache
(def request-cache
  (atom {}))

(defn cache-request
  "Caches a request result with timestamp"
  [key data]
  (swap! request-cache assoc key 
         {:data data 
          :timestamp (System/currentTimeMillis)}))

(defn get-cached
  "Gets cached data if not expired (30 seconds)"
  [key]
  (when-let [cached (get @request-cache key)]
    (when (< (- (System/currentTimeMillis) 
                (:timestamp cached)) 
             30000)
      (:data cached))))

;; 2. Refs for Resource Pool
(def connection-pool
  (ref {:available #{:conn1 :conn2 :conn3}
        :in-use #{}}))

(defn acquire-connection
  "Acquires a connection from the pool"
  []
  (dosync
    (when-let [conn (first (:available @connection-pool))]
      (alter connection-pool 
             (fn [pool]
               (-> pool
                   (update :available disj conn)
                   (update :in-use conj conn))))
      conn)))

(defn release-connection
  "Releases a connection back to the pool"
  [conn]
  (dosync
    (alter connection-pool
           (fn [pool]
             (-> pool
                 (update :in-use disj conn)
                 (update :available conj conn))))))

;; === Examples and Demonstrations ===
(defn run-examples []
  (println "\n=== Game Score Example (Atom) ===")
  (reset-game)
  (update-score :player1 100)
  (update-score :player2 50)
  (lose-life :player1)
  (println "Game State:")
  (pp/pprint @game-state)

  (println "\n=== Banking Example (Refs) ===")
  (println "Initial balances:")
  (println "Checking:" @checking-account)
  (println "Savings:" @savings-account)
  
  (transfer checking-account savings-account 500)
  (println "\nAfter transfer:")
  (println "Checking:" @checking-account)
  (println "Savings:" @savings-account)

  (println "\n=== Shopping Cart Example (Refs) ===")
  (dosync (ref-set inventory {:apples 10 :oranges 5}))
  (dosync (ref-set shopping-cart {}))
  
  (println "Initial inventory:" @inventory)
  (add-to-cart :apples 3)
  (add-to-cart :oranges 2)
  (println "\nAfter adding to cart:")
  (println "Inventory:" @inventory)
  (println "Cart:" @shopping-cart)
  
  (return-item :apples 1)
  (println "\nAfter returning item:")
  (println "Inventory:" @inventory)
  (println "Cart:" @shopping-cart)

  (println "\n=== Resource Pool Example (Refs) ===")
  (let [conn1 (acquire-connection)
        conn2 (acquire-connection)]
    (println "After acquiring connections:")
    (pp/pprint @connection-pool)
    
    (release-connection conn1)
    (println "\nAfter releasing connection:")
    (pp/pprint @connection-pool)))

(run-examples)