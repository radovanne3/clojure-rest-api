(ns snorlax.api-playground
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

;; Here we will create new atom called people-collection, which will store vector of people. (mutable collection)
(def people-collection (atom []))


(defn getparameter
  [req param-name]
  (param-name (:params req)))

;; simple-body-page
(defn simple-body-page
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

;; request-example
(defn request-example
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (->>
           (pp/pprint req)
           (str "Request Object: " req))})

;; hello-name
(defn hello-name
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (->
           (pp/pprint req)
           (str "Hello " (:firstname (:params req)) " " (:lastname (:params req))))})

;; Return list of people
(defn people-listing
  "Returns list of all people inside people-collection"
  [req]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (str (json/write-str @people-collection))})
;; This function takes our collection and converts all of the mapped key value pairs into JSON.

;; Here we will make a clear people-collection function
(defn reset-people-collection
  [req]
  (do
    (reset! people-collection [])
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body "You have successfully deleted all people from list!" }))


;; Here we will create helper function that will take first-name and last-name, create new person map and add it to people-collection with capitalized first letters of both.

(defn add-person
  "Adds new person to the people-collection"
  [first-name last-name]
  (swap! people-collection conj {:firstname (str/capitalize first-name) :lastname (str/capitalize last-name)}))


;; We are going to partially apply the getparameter function, by passing it the req object. We can then assign this to a local variable p which will allow us to retrieve any key in the :params map simply by calling (p :firstname)

(defn add-person-handler
  [req]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (-> (let [p (partial getparameter req)]
               (str (json/write-str (add-person (p :firstname) (p :lastname))))))})

;; Here we will enable rest-api users to update our people-collection atom, they will send request and will will extract info from nested :params


;; Here we will define routes:
(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET "/request" [] request-example)
  (GET "/hello" [] hello-name)
  (GET "/people" [] people-listing)
  (GET "/people/add" [] add-person-handler)
  (GET "/people/reset" [] reset-people-collection)
  (route/not-found "Error, page not found!"))


;; 1. Our main function should run HTTPKit SERVER
(defn -main
  "MAIN ENTRY POINT."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ;; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults)
                       {:port port})
    ;; Run the server without ring defaults
    ;; (server/run-server #'app-routes {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))


;; CODE TESTING AREA

#_(Integer/parseInt "3000")
;; => 3000 -> our port variable value (System/getenv "PORT") returns nil? (CHECK THIS!)

;; Why do we use #' ???
#_#'app-routes
;;app-routes

#_@people-collection
;; @ is symbol for ""deref""

;; (add-person "Nedeljko" "Radovanovic")
;; (add-person "Mark" "Adams")






























