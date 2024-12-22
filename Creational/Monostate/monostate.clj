(ns monostate)

;; Defines the global state for the session with private visibility
(def ^:private session-state
  (atom {:user-id nil
         :permissions #{}
         :last-access nil}))

;; Function to start a session
(defn start-session
  "Starts a new user session with ID and permissions."
  [user-id permissions]
  (reset! session-state {:user-id user-id
                         :permissions permissions
                         :last-access (java.time.Instant/now)}))

;; Function to end the session
(defn end-session
  "Ends the session, resetting the state to default values."
  []
  (reset! session-state {:user-id nil
                         :permissions #{}
                         :last-access nil}))

;; Function to update the last access timestamp
(defn update-last-access
  "Updates the last access timestamp to the current time."
  []
  (swap! session-state assoc :last-access (java.time.Instant/now)))

;; Function to retrieve the current session state
(defn get-session
  "Retrieves the complete state of the current session."
  []
  @session-state)

;; Function to check user permissions
(defn has-permission?
  "Checks if the user has a specific permission."
  [permission]
  (contains? (:permissions @session-state) permission))

;; Example usage in a comment
(comment
  ;; Starts a new session
  (start-session "user-123" #{"read" "write"})
  ;; => {:user-id "user-123", :permissions #{"read" "write"}, :last-access <timestamp>}

  ;; Checks permissions
  (has-permission? "read") ;; => true
  (has-permission? "delete") ;; => false

  ;; Updates the last access timestamp
  (update-last-access)

  ;; Retrieves the complete session state
  (get-session)
  ;; => {:user-id "user-123", :permissions #{"read" "write"}, :last-access <new-timestamp>}

  ;; Ends the session
  (end-session)
  ;; => {:user-id nil, :permissions #{}, :last-access nil}
)
