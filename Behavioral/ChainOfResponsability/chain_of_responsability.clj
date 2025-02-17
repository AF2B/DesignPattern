(ns chain-of-responsibility
  (:require [clojure.pprint :as pp]))

(defprotocol RequestHandler
  (handle-request [this request])
  (set-next [this handler]))

(defrecord AuthenticationHandler [next-handler]
  RequestHandler
  (handle-request [this request]
    (if-let [auth-token (:auth-token request)]
      (if (= auth-token "valid-token")
        (if next-handler
          (handle-request next-handler 
                         (assoc request :authenticated true))
          (assoc request :authenticated true))
        {:error "Invalid authentication token"})
      {:error "Missing authentication token"}))

  (set-next [_ handler]
    (->AuthenticationHandler handler)))

(defrecord AuthorizationHandler [next-handler]
  RequestHandler
  (handle-request [this request]
    (if (:authenticated request)
      (if (contains? (:roles request) :admin)
        (if next-handler
          (handle-request next-handler 
                         (assoc request :authorized true))
          (assoc request :authorized true))
        {:error "Insufficient permissions"})
      (if next-handler
        (handle-request next-handler request)
        request)))

  (set-next [_ handler]
    (->AuthorizationHandler handler)))

(defrecord ValidationHandler [next-handler]
  RequestHandler
  (handle-request [this request]
    (if (and (:data request)
             (map? (:data request))
             (every? string? (vals (:data request))))
      (if next-handler
        (handle-request next-handler 
                       (assoc request :validated true))
        (assoc request :validated true))
      {:error "Invalid request data format"}))

  (set-next [_ handler]
    (->ValidationHandler handler)))

(defrecord LoggingHandler [next-handler]
  RequestHandler
  (handle-request [this request]
    (println "\nProcessing request:")
    (pp/pprint (dissoc request :handler))
    (let [response (if next-handler
                    (handle-request next-handler request)
                    request)]
      (println "\nResponse:")
      (pp/pprint response)
      response))

  (set-next [_ handler]
    (->LoggingHandler handler)))

(def request-cache (atom {}))

(defrecord CacheHandler [next-handler]
  RequestHandler
  (handle-request [this request]
    (if-let [cached (@request-cache (:id request))]
      (do
        (println "Cache hit for request:" (:id request))
        cached)
      (let [response (if next-handler
                      (handle-request next-handler request)
                      request)]
        (when (:id request)
          (swap! request-cache assoc (:id request) response))
        response)))

  (set-next [_ handler]
    (->CacheHandler handler)))

(defn build-chain []
  (-> (->LoggingHandler nil)
      (set-next (->CacheHandler nil))
      (set-next (->AuthenticationHandler nil))
      (set-next (->AuthorizationHandler nil))
      (set-next (->ValidationHandler nil))))

(defn run-examples []
  (let [chain (build-chain)]
    (println "\n=== Valid Admin Request ===")
    (handle-request chain
                   {:id "req-1"
                    :auth-token "valid-token"
                    :roles #{:admin}
                    :data {"name" "John"
                          "action" "read"}})

    (println "\n=== Invalid Token ===")
    (handle-request chain
                   {:id "req-2"
                    :auth-token "invalid-token"
                    :roles #{:admin}
                    :data {"name" "John"}})

    (println "\n=== Missing Token ===")
    (handle-request chain
                   {:id "req-3"
                    :roles #{:admin}
                    :data {"name" "John"}})

    (println "\n=== Insufficient Permissions ===")
    (handle-request chain
                   {:id "req-4"
                    :auth-token "valid-token"
                    :roles #{:user}
                    :data {"name" "John"}})

    (println "\n=== Invalid Data ===")
    (handle-request chain
                   {:id "req-5"
                    :auth-token "valid-token"
                    :roles #{:admin}
                    :data {"name" 123}})

    (println "\n=== Cached Request ===")
    (handle-request chain
                   {:id "req-1"
                    :auth-token "valid-token"
                    :roles #{:admin}
                    :data {"name" "John"
                          "action" "read"}})))

(run-examples)