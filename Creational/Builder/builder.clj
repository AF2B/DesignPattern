(ns builder)

(defn create-report
  "Creates an initial structure for the report."
  []
  {:title    nil
   :author   nil
   :date     nil
   :content  []
   :summary  nil})

(defn set-title
  "Sets the title of the report."
  [report title]
  (assoc report :title title))

(defn set-author
  "Sets the author of the report."
  [report author]
  (assoc report :author author))

(defn set-date
  "Sets the date of the report."
  [report date]
  (assoc report :date date))

(defn add-content
  "Adds a section to the report content."
  [report section]
  (update report :content conj section))

(defn set-summary
  "Adds a summary to the report."
  [report summary]
  (assoc report :summary summary))

(defn build-report
  "Validates and returns the finalized report."
  [report]
  (if (and (:title report) (:author report) (:content report))
    report
    (throw (IllegalArgumentException. "Invalid report: title, author, and content are mandatory."))))

(defn generate-sample-report []
  (-> (create-report)
      (set-title "Annual Financial Report")
      (set-author "Finance Department")
      (set-date "2024-12-13")
      (add-content "Introduction to financial results.")
      (add-content "Analysis of expenses and revenues.")
      (set-summary "This report provides a detailed overview of the annual financial performance.")
      (build-report)))

(comment
  (generate-sample-report)
  ;; => {:title "Annual Financial Report", 
  ;;     :author "Finance Department", 
  ;;     :date "2024-12-13", 
  ;;     :content ["Introduction to financial results." "Analysis of expenses and revenues."], 
  ;;     :summary "This report provides a detailed overview of the annual financial performance."}
)
