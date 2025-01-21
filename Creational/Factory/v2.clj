(ns v2
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::distance (s/and number? pos?))
(s/def ::package string?)
(s/def ::destination string?)
(s/def ::delivery-type #{"air" "land" "sea"})

(defprotocol DeliveryService
  (calculate-cost [this distance] "Calculates the delivery cost based on the distance.")
  (process-delivery [this package destination] "Executes the delivery of the package to the destination.")
  (estimated-time [this distance] "Estimates delivery time in hours."))

(defrecord AirDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 5 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Air to " destination))
  (estimated-time [_ distance]
    (Math/ceil (/ distance 800))))

(defrecord LandDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 2 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Land to " destination))
  (estimated-time [_ distance]
    (Math/ceil (/ distance 60))))

(defrecord SeaDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 1 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Sea to " destination))
  (estimated-time [_ distance]
    (Math/ceil (/ distance 30))))

(defmulti create-delivery-service
  "Factory multi-method for creating delivery services"
  (fn [type & _] (str/lower-case type)))

(defmethod create-delivery-service "air" [_]
  (->AirDelivery))

(defmethod create-delivery-service "land" [_]
  (->LandDelivery))

(defmethod create-delivery-service "sea" [_]
  (->SeaDelivery))

(defmethod create-delivery-service :default [type]
  (throw (ex-info "Invalid delivery type"
                  {:type type
                   :available-types #{"air" "land" "sea"}})))

(defn calculate-and-deliver
  "Service that uses the factory to calculate the cost and deliver a package.
   Returns a map with :cost, :delivery, and :estimated-time keys."
  [type package destination distance]
  {:pre [(s/valid? ::delivery-type type)
         (s/valid? ::package package)
         (s/valid? ::destination destination)
         (s/valid? ::distance distance)]}
  (try
    (let [service (create-delivery-service type)]
      {:cost (calculate-cost service distance)
       :delivery (process-delivery service package destination)
       :estimated-time (estimated-time service distance)})
    (catch Exception e
      (throw (ex-info "Delivery calculation failed"
                      {:cause (.getMessage e)
                       :type type
                       :package package
                       :destination destination
                       :distance distance})))))

(defn find-cheapest-delivery
  "Finds the cheapest delivery service for given parameters"
  [package destination distance]
  (->> ["air" "land" "sea"]
       (map #(-> [(calculate-and-deliver % package destination distance) %]))
       (sort-by (comp :cost first))
       first))

(comment
  (calculate-and-deliver "air" "Electronics" "São Paulo" 1000)
  ;; => {:cost 5000
  ;;     :delivery "Package 'Electronics' will be delivered via Air to São Paulo"
  ;;     :estimated-time 2}
  
  (find-cheapest-delivery "Heavy Machinery" "Porto Alegre" 800)
  ;; => [{:cost 800
  ;;      :delivery "Package 'Heavy Machinery' will be delivered via Sea to Porto Alegre"
  ;;      :estimated-time 27}
  ;;     "sea"]
  
  ;; Validation error example
  (calculate-and-deliver "air" "Books" "Curitiba" -100)
  ;; => Assertion Error: Invalid input
)