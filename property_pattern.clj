(ns property-pattern
  (:require [clojure.spec.alpha :as s]))

(s/def ::subscription-active boolean?)
(s/def ::plan-type #{"Basic" "Premium" "Enterprise"})
(s/def ::user (s/keys :req-un [::subscription-active ::plan-type]))

(defn matches-properties?
  "Checks if a map satisfies a set of expected properties.
   Args:
     data - Input map to be checked.
     expected - Map with the expected properties.
   Returns:
     Boolean indicating if all properties match."
  [data expected]
  (every? (fn [[k v]]
            (= (get data k) v))
          expected))

(defn process-user
  "Processes a user based on their properties, applying the corresponding logic.
   Args:
     user - Map representing the user.
   Returns:
     String with the processing result or an error message if the user data is invalid"
  [user]
  (if (s/valid? ::user user)
    (cond
      (matches-properties? user {:subscription-active true :plan-type "Premium"})
      "Welcome, Premium user!"

      (matches-properties? user {:subscription-active true :plan-type "Enterprise"})
      "Welcome, Enterprise user with exclusive benefits!"

      :else
      "Inactive subscription or basic plan. Consider upgrading!")
    (str "Invalid user data: " (s/explain-data ::user user))))