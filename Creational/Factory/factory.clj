(ns factory
  (:require [clojure.string :as str]))

(defprotocol DeliveryService
  "Interface for delivery services."
  (calculate-cost [this distance] "Calculates the delivery cost based on the distance.")
  (process-delivery [this package destination] "Executes the delivery of the package to the destination."))

(defrecord AirDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 5 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Air to " destination)))

(defrecord LandDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 2 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Land to " destination)))

(defrecord SeaDelivery []
  DeliveryService
  (calculate-cost [_ distance]
    (* 1 distance))
  (process-delivery [_ package destination]
    (str "Package '" package "' will be delivered via Sea to " destination)))

(defn delivery-factory
  "Factory that creates a delivery service based on the given type."
  [type]
  (case (str/lower-case type)
    "air"  (->AirDelivery)
    "land" (->LandDelivery)
    "sea"  (->SeaDelivery)
    (throw (IllegalArgumentException.
            (str "Invalid delivery type: " type)))))

(defn calculate-and-deliver
  "Service that uses the factory to calculate the cost and deliver a package."
  [type package destination distance]
  (let [service (delivery-factory type)]
    {:cost (calculate-cost service distance)
     :delivery (process-delivery service package destination)}))

(comment
  (calculate-and-deliver "air" "Box of Parts" "Pernambuco" 100)
  ;; => {:cost 500, :delivery "Package 'Box of Parts' will be delivered via Air to Pernambuco"}

  (calculate-and-deliver "land" "Toolbox" "Rio Grande Do Sul" 50)
  ;; => {:cost 100, :delivery "Package 'Toolbox' will be delivered via Land to Rio Grande Do Sul"}

  (calculate-and-deliver "space" "Heavy Cargo" "Port of Santos" 200)
  ;; => IllegalArgumentException: Invalid delivery type: space
)
