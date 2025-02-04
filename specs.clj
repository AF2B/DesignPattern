(ns specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::user-id uuid?)
(s/def ::email (s/and string? #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$" %)))
(s/def ::username (s/and string? #(re-matches #"^[a-zA-Z0-9_-]{3,20}$" %)))
(s/def ::password (s/and string? #(>= (count %) 8)))
(s/def ::age (s/int-in 13 130))
(s/def ::roles #{:admin :user :moderator})

(s/def ::user (s/keys :req-un [::user-id ::email ::username ::password ::age]
                      :opt-un [::roles]))

(s/fdef create-user
  :args (s/cat :user-data ::user)
  :ret (s/or :success ::user
             :failure ::error-response))

(s/def ::error-response (s/keys :req-un [::error ::message]))
(s/def ::error keyword?)
(s/def ::message string?)

(defn validate-email [email]
  (s/valid? ::email email))

(defn validate-username [username]
  (s/valid? ::username username))

(defn validate-password [password]
  (s/valid? ::password password))

(defn validate-age [age]
  (s/valid? ::age age))

(defn validate-user [user]
  (s/valid? ::user user))

(def sample-user
  {:user-id (random-uuid)
   :email "user@example.com"
   :username "john_doe123"
   :password "secure_password123"
   :age 25
   :roles #{:user}})

(def invalid-user
  {:user-id (random-uuid)
   :email "invalid-email"
   :username "j"  ;; too short
   :password "123" ;; too short
   :age 10})      ;; too young

(defn validate-and-explain [spec data]
  (if (s/valid? spec data)
    (println "Valid:" data)
    (do
      (println "Invalid:" data)
      (println "Explanation:")
      (s/explain spec data))))

(comment
  (defn run-examples []
  (println "\n=== Email Validation ===")
  (println "Valid email:" (validate-email "user@example.com"))
  (println "Invalid email:" (validate-email "invalid-email"))

  (println "\n=== Username Validation ===")
  (println "Valid username:" (validate-username "john_doe123"))
  (println "Invalid username:" (validate-username "a"))  ;; too short

  (println "\n=== Password Validation ===")
  (println "Valid password:" (validate-password "secure_password123"))
  (println "Invalid password:" (validate-password "123"))  ;; too short

  (println "\n=== Age Validation ===")
  (println "Valid age:" (validate-age 25))
  (println "Invalid age:" (validate-age 10))  ;; too young

  (println "\n=== Complete User Validation ===")
  (println "Valid User:")
  (validate-and-explain ::user sample-user)

  (println "\nInvalid User:")
  (validate-and-explain ::user invalid-user))

(run-examples))