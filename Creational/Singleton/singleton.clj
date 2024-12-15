(ns singleton)

(defprotocol LoggerService
  "Protocol defining logging operations."
  (log-info [this message] "Logs an informational message.")
  (log-error [this message] "Logs an error message.")
  (log-debug [this message] "Logs a debug message."))

(defrecord FileLogger [log-file]
  LoggerService
  (log-info [_ message]
    (spit log-file (str (java.time.Instant/now) " [INFO]: " message "\n") :append true))
  (log-error [_ message]
    (spit log-file (str (java.time.Instant/now) " [ERROR]: " message "\n") :append true))
  (log-debug [_ message]
    (spit log-file (str (java.time.Instant/now) " [DEBUG]: " message "\n") :append true)))

(defonce logger-instance
  (atom nil))

(defn get-logger
  "Returns the singleton instance of the LoggerService."
  []
  (if-let [instance @logger-instance]
    instance
    (let [new-instance (->FileLogger "application.log")]
      (reset! logger-instance new-instance)
      new-instance)))

(defn log-endpoint
  "A Pedestal handler that logs requests and responses."
  [request]
  (let [logger (get-logger)]
    (log-info logger (str "Received request: " (:uri request)))
    {:status 200
     :body   "Request logged successfully!"}))

(require '[io.pedestal.http :as http])

(def service
  {:env                  :prod
   ::http/routes         #{["/log" :get log-endpoint]}
   ::http/type           :jetty
   ::http/port           8080})

(comment
  ;; Start the server
  (http/create-server service)
  ;; curl http://localhost:8080/log
)
