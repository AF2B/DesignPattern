(ns command
  (:require [clojure.spec.alpha :as s]))

(s/def ::position (s/and int? (complement neg?)))
(s/def ::length (s/and int? (complement neg?)))
(s/def ::text string?)

(defprotocol Command
  "Protocol defining an executable and reversible operation"
  (execute [this] "Performs the command, mutating its receiver")
  (undo [this] "Reverts the effect of a previously executed command"))

(defrecord InsertTextCommand [document-atom position text]
  Command
  (execute [_]
    {:pre [(s/valid? ::position position) (s/valid? ::text text)]}
    (swap! document-atom
           (fn [doc] (str (subs doc 0 position) text (subs doc position)))))
  (undo [_]
    (swap! document-atom
           (fn [doc]
             (str (subs doc 0 position)
                  (subs doc (+ position (count text))))))))

(defrecord DeleteTextCommand [document-atom position length deleted-atom]
  Command
  (execute [_]
    {:pre [(s/valid? ::position position) (s/valid? ::length length)]}
    (swap! document-atom
           (fn [doc]
             (reset! deleted-atom (subs doc position (+ position length)))
             (str (subs doc 0 position) (subs doc (+ position length))))))
  (undo [_]
    (swap! document-atom
           (fn [doc]
             (str (subs doc 0 position) @deleted-atom (subs doc position))))))

(defn create-delete-command
  "Builds a DeleteTextCommand, wiring up the scratch atom undo needs to
   remember what was deleted"
  [document-atom position length]
  (->DeleteTextCommand document-atom position length (atom nil)))

(defrecord MacroCommand [commands]
  Command
  (execute [_]
    (doseq [cmd commands] (execute cmd)))
  (undo [_]
    (doseq [cmd (reverse commands)] (undo cmd))))

(defn create-invoker
  "Creates a new, empty command history"
  []
  (atom []))

(defn run-command!
  "Executes a command through the invoker and pushes it onto the history
   stack so it can be undone later"
  [invoker cmd]
  (execute cmd)
  (swap! invoker conj cmd)
  cmd)

(defn undo-last!
  "Pops and undoes the most recently executed command, if any"
  [invoker]
  (when-let [cmd (peek @invoker)]
    (undo cmd)
    (swap! invoker pop)
    cmd))

(comment
  (def document (atom "Hello, world!"))
  (def invoker (create-invoker))

  (run-command! invoker (->InsertTextCommand document 5 " there"))
  @document
  ;; => "Hello there, world!"

  (run-command! invoker (create-delete-command document 0 6))
  @document
  ;; => "there, world!"

  (undo-last! invoker)
  @document
  ;; => "Hello there, world!"

  (undo-last! invoker)
  @document
  ;; => "Hello, world!"

  ;; Commands compose: a MacroCommand executes/undoes several commands as one
  (def paragraph (atom ""))
  (def add-title
    (->MacroCommand [(->InsertTextCommand paragraph 0 "Title\n")
                      (->InsertTextCommand paragraph 6 "Body text.")]))
  (execute add-title)
  @paragraph
  ;; => "Title\nBody text."
  (undo add-title)
  @paragraph
  ;; => ""
  )

(comment
  (require '[clojure.test :refer [deftest testing is]])

  (deftest command-pattern-test
    (testing "Insert executes and undoes"
      (let [doc (atom "ac")
            cmd (->InsertTextCommand doc 1 "b")]
        (execute cmd)
        (is (= @doc "abc"))
        (undo cmd)
        (is (= @doc "ac"))))

    (testing "Delete remembers the removed text for undo"
      (let [doc (atom "abc")
            cmd (create-delete-command doc 1 1)]
        (execute cmd)
        (is (= @doc "ac"))
        (undo cmd)
        (is (= @doc "abc"))))

    (testing "Invoker history supports multi-step undo"
      (let [doc (atom "")
            invoker (create-invoker)]
        (run-command! invoker (->InsertTextCommand doc 0 "foo"))
        (run-command! invoker (->InsertTextCommand doc 3 "bar"))
        (is (= @doc "foobar"))
        (undo-last! invoker)
        (is (= @doc "foo"))
        (undo-last! invoker)
        (is (= @doc ""))))

    (testing "MacroCommand executes and undoes as a single unit"
      (let [doc (atom "")
            macro (->MacroCommand [(->InsertTextCommand doc 0 "A")
                                    (->InsertTextCommand doc 1 "B")])]
        (execute macro)
        (is (= @doc "AB"))
        (undo macro)
        (is (= @doc ""))))))
