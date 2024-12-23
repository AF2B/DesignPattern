(ns monostate-test
  (:require
   [clojure.test :refer [is testing deftest run-tests]]
   [monostate :refer [get-session 
                      start-session 
                      end-session 
                      update-last-access
                      has-permission?]]))

(deftest monostate-tests
  (testing "should start session"
    (let [user-1 {:user-id 27
                  :permissions #{:read}}]
      (start-session (:user-id user-1) (:permissions user-1))
      (let [session-state (get-session)]
        (is (= (:user-id user-1) (:user-id session-state)))
        (is (= (:permissions user-1) (:permissions session-state)))
        (is (some? (:last-access session-state))))))
  
  (testing "should end session"
    (let [default-session {:user-id nil
                           :permissions #{}
                           :last-access nil}]
      (end-session)
      (is (= default-session (get-session)))))
  
  (testing "should update the last access"
    (let [user-1 {:user-id 27
                  :permissions #{:read}}]
      (start-session (:user-id user-1) (:permissions user-1))
      
      (let [initial-session (get-session)
            initial-last-access (:last-access initial-session)]
        
        (is (some? initial-last-access))

        (Thread/sleep 10)
        
        (update-last-access)
        
        (let [updated-session (get-session)
              updated-last-access (:last-access updated-session)]
          
          (is (not= initial-last-access updated-last-access))
          
          (is (instance? java.time.Instant updated-last-access))))))
  
  (testing "should contains permission"
    (let [user-1 {:user-id 27
                  :permissions #{:read :write}}]
      (start-session (:user-id user-1) (:permissions user-1))
      (is (has-permission? :write))
      (is (has-permission? :read))))
  
  (testing "should not contains permission"
    (let [user-1 {:user-id 27
                  :permissions #{:write}}]
      (start-session (:user-id user-1) (:permissions user-1))
      (is (not (has-permission? :delete))))))

(run-tests)
