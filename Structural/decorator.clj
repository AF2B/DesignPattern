(ns decorator
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)
(s/def ::items (s/coll-of map? :kind vector?))
(s/def ::total (s/and number? pos?))
(s/def ::customer-id string?)
(s/def ::order (s/keys :req-un [::id ::items ::total ::customer-id]))

(defprotocol OrderProcessor
  "Protocol defining order processing operations"
  (process-order [this order] "Processes an order and returns the processed result")
  (get-order [this order-id] "Retrieves an order by its ID")
  (cancel-order [this order-id] "Cancels an order by its ID"))

(defrecord BasicOrderProcessor []
  OrderProcessor
  (process-order [_ order]
    {:status :processed
     :order order
     :processed-at (java.time.Instant/now)})
  
  (get-order [_ order-id]
    {:status :retrieved
     :order-id order-id
     :retrieved-at (java.time.Instant/now)})
  
  (cancel-order [_ order-id]
    {:status :cancelled
     :order-id order-id
     :cancelled-at (java.time.Instant/now)}))

(defrecord ValidationDecorator [processor]
  OrderProcessor
  (process-order [_ order]
    (if (s/valid? ::order order)
      (process-order processor order)
      (throw (ex-info "Invalid order structure"
                     {:error (s/explain-str ::order order)}))))
  
  (get-order [_ order-id]
    (if (string? order-id)
      (get-order processor order-id)
      (throw (ex-info "Invalid order ID" {:order-id order-id}))))
  
  (cancel-order [_ order-id]
    (if (string? order-id)
      (cancel-order processor order-id)
      (throw (ex-info "Invalid order ID" {:order-id order-id})))))

(defrecord LoggingDecorator [processor]
  OrderProcessor
  (process-order [_ order]
    (println "Processing order:" (:id order))
    (let [result (process-order processor order)]
      (println "Order processed:" (:id order))
      result))
  
  (get-order [_ order-id]
    (println "Retrieving order:" order-id)
    (let [result (get-order processor order-id)]
      (println "Order retrieved:" order-id)
      result))
  
  (cancel-order [_ order-id]
    (println "Cancelling order:" order-id)
    (let [result (cancel-order processor order-id)]
      (println "Order cancelled:" order-id)
      result)))

(defrecord CachingDecorator [processor]
  OrderProcessor
  (process-order [_ order]
    (let [cache (atom {})]
      (if-let [cached (get @cache (:id order))]
        cached
        (let [result (process-order processor order)]
          (swap! cache assoc (:id order) result)
          result))))
  
  (get-order [_ order-id]
    (let [cache (atom {})]
      (if-let [cached (get @cache order-id)]
        cached
        (let [result (get-order processor order-id)]
          (swap! cache assoc order-id result)
          result))))
  
  (cancel-order [_ order-id]
    (cancel-order processor order-id)))

(defrecord MetricsDecorator [processor]
  OrderProcessor
  (process-order [_ order]
    (let [start-time (System/nanoTime)
          result (process-order processor order)
          end-time (System/nanoTime)
          duration (/ (- end-time start-time) 1e6)]
      (println "Order processing time:" duration "ms")
      result))
  
  (get-order [_ order-id]
    (let [start-time (System/nanoTime)
          result (get-order processor order-id)
          end-time (System/nanoTime)
          duration (/ (- end-time start-time) 1e6)]
      (println "Order retrieval time:" duration "ms")
      result))
  
  (cancel-order [_ order-id]
    (let [start-time (System/nanoTime)
          result (cancel-order processor order-id)
          end-time (System/nanoTime)
          duration (/ (- end-time start-time) 1e6)]
      (println "Order cancellation time:" duration "ms")
      result)))

(defn create-order-processor
  "Creates a new order processor with the specified decorators.
   Available decorators: :validation, :logging, :caching, :metrics"
  [& decorators]
  (let [base-processor (->BasicOrderProcessor)]
    (reduce (fn [processor decorator]
              (case decorator
                :validation (->ValidationDecorator processor)
                :logging (->LoggingDecorator processor)
                :caching (->CachingDecorator processor)
                :metrics (->MetricsDecorator processor)
                processor))
            base-processor
            decorators)))

(comment
  (def processor 
    (create-order-processor :validation :logging :caching :metrics))

  (def sample-order
    {:id "ORDER-123"
     :items [{:product-id "PROD-1" :quantity 2 :price 10.0}
             {:product-id "PROD-2" :quantity 1 :price 15.0}]
     :total 35.0
     :customer-id "CUST-456"})

  (process-order processor sample-order)

  (get-order processor "ORDER-123")

  (cancel-order processor "ORDER-123")

  (process-order processor (dissoc sample-order :total)))