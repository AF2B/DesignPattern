(ns facade
  (:require [clojure.spec.alpha :as s]))

(s/def ::order-id uuid?)
(s/def ::customer-id uuid?)
(s/def ::product-id uuid?)
(s/def ::quantity pos-int?)
(s/def ::price decimal?)
(s/def ::payment-method #{:credit-card :debit-card :bank-transfer})
(s/def ::item (s/keys :req-un [::product-id ::quantity ::price]))
(s/def ::items (s/coll-of ::item :min-count 1))
(s/def ::order (s/keys :req-un [::order-id ::customer-id ::items ::payment-method]))

(defprotocol InventorySystem
  (check-stock [this item])
  (reserve-items [this items])
  (release-items [this items]))

(defprotocol PaymentSystem
  (process-payment [this order amount])
  (refund-payment [this order amount]))

(defprotocol ShippingSystem
  (calculate-shipping [this items])
  (create-shipment [this order]))

(defprotocol NotificationSystem
  (notify-customer [this customer-id message]))

(defrecord Inventory []
  InventorySystem
  (check-stock [_ item]
    (let [available-quantity 100]
      (>= available-quantity (:quantity item))))
  
  (reserve-items [_ items]
    {:success true
     :reserved-items items})
  
  (release-items [_ items]
    {:success true
     :released-items items}))

(defrecord Payment []
  PaymentSystem
  (process-payment [_ order amount]
    {:success true
     :transaction-id (random-uuid)
     :amount amount})
  
  (refund-payment [_ order amount]
    {:success true
     :refund-id (random-uuid)
     :amount amount}))

(defrecord Shipping []
  ShippingSystem
  (calculate-shipping [_ items]
    (* (count items) 10.0))
  
  (create-shipment [_ order]
    {:success true
     :shipment-id (random-uuid)
     :tracking-number (str "TRACK-" (random-uuid))}))

(defrecord Notification []
  NotificationSystem
  (notify-customer [_ customer-id message]
    {:success true
     :notification-id (random-uuid)
     :customer-id customer-id
     :message message}))

(defprotocol OrderProcessingFacade
  (process-order [this order])
  (cancel-order [this order]))

(defrecord OrderProcessor [inventory payment shipping notification]
  OrderProcessingFacade
  (process-order [_ order]
    (try
      (if-not (s/valid? ::order order)
        (throw (ex-info "Invalid order structure" 
                       (s/explain-data ::order order)))
        (let [items (:items order)
              shipping-cost (calculate-shipping shipping items)
              total-amount (+ (reduce + (map #(* (:quantity %) (:price %)) items))
                             shipping-cost)]
          
          (when-not (every? #(check-stock inventory %) items)
            (throw (ex-info "Insufficient stock" {:items items})))
          
          (let [inventory-result (reserve-items inventory items)
                payment-result (process-payment payment order total-amount)
                shipment-result (create-shipment shipping order)]
            
            (notify-customer notification 
                           (:customer-id order)
                           (str "Order " (:order-id order) " processed successfully. "
                                "Tracking number: " (:tracking-number shipment-result)))
            
            {:success true
             :order-id (:order-id order)
             :payment payment-result
             :shipment shipment-result
             :total-amount total-amount})))
      
      (catch Exception e
        {:success false
         :error (.getMessage e)
         :details (ex-data e)})))
  
  (cancel-order [_ order]
    (try
      (let [items (:items order)
            shipping-cost (calculate-shipping shipping items)
            total-amount (+ (reduce + (map #(* (:quantity %) (:price %)) items))
                           shipping-cost)]
        
        (release-items inventory items)
        (refund-payment payment order total-amount)
        
        (notify-customer notification
                         (:customer-id order)
                         (str "Order " (:order-id order) " cancelled successfully."))
        
        {:success true
         :order-id (:order-id order)
         :refund-amount total-amount})
      
      (catch Exception e
        {:success false
         :error (.getMessage e)
         :details (ex-data e)}))))

(defn create-order-processor
  "Creates a new instance of the Order Processing Facade"
  []
  (->OrderProcessor
   (->Inventory)
   (->Payment)
   (->Shipping)
   (->Notification)))

(comment
  (def processor (create-order-processor))
  
  (def sample-order
    {:order-id (random-uuid)
     :customer-id (random-uuid)
     :items [{:product-id (random-uuid)
              :quantity 2
              :price 29.99}
             {:product-id (random-uuid)
              :quantity 1
              :price 49.99}]
     :payment-method :credit-card})
  
  (process-order processor sample-order)
  
  (cancel-order processor sample-order)
  
  (process-order processor (dissoc sample-order :items))
)

(comment
  (require '[clojure.test :refer [deftest testing is]])
  
  (deftest order-processing-facade-test
    (let [processor (create-order-processor)
          valid-order {:order-id (random-uuid)
                      :customer-id (random-uuid)
                      :items [{:product-id (random-uuid)
                              :quantity 1
                              :price 100.0}]
                      :payment-method :credit-card}]
      
      (testing "Process valid order"
        (let [result (process-order processor valid-order)]
          (is (:success result))
          (is (:shipment result))
          (is (:payment result))))
      
      (testing "Process invalid order"
        (let [invalid-order (dissoc valid-order :items)
              result (process-order processor invalid-order)]
          (is (not (:success result)))
          (is (:error result))))
      
      (testing "Cancel order"
        (let [result (cancel-order processor valid-order)]
          (is (:success result))
          (is (= (:order-id result) (:order-id valid-order)))))))
)