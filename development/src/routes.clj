(ns routes
  (:require
   [io.pedestal.http.route :as route]
   [io.pedestal.http.route.definition.table :refer [table-routes]]
   [poly.web.rest-api.api :as api]))

  ;; Utility functions from http://pedestal.io/guides/developing-at-the-repl#developing-at-the-repl

(defn print-routes
  "Print our application's routes"
  []
  (route/print-routes (table-routes api/routes)))

(defn named-route
  "Finds a route by name"
  [route-name]
  (->> api/routes
       table-routes
       (filter #(= route-name (:route-name %)))
       first))

(defn recognize-route
  "Verifies the requested HTTP verb and path are recognized by the router."
  [verb path]
  (route/try-routing-for (table-routes api/routes) :prefix-tree path verb))

(defn print-route
  "Prints a route and its interceptors"
  [rname]
  (letfn [(joined-by
            [s coll]
            (apply str (interpose s coll)))

          (repeat-str
            [s n]
            (apply str (repeat n s)))

          (interceptor-info
            [i]
            (let [iname  (or (:name i) "<handler>")
                  stages (->> (dissoc i :name)
                              (filter (comp (complement nil?) val))
                              keys
                              (joined-by ","))]
              (str iname " (" stages ")")))]
    (when-let [rte (named-route rname)]
      (let [{:keys [path method route-name interceptors]} rte
            name-line (str "[" method " " path " " route-name "]")]
        (->> (into [name-line (repeat-str "-" (count name-line))]
                   (map interceptor-info interceptors))
             (joined-by "\n")
             println)))))

(comment
  ;; print all routes
  (print-routes)

  ;; find a specific route
  (named-route :user-register)

  ;; get detailed route info by handler name
  (print-route :user-register)

  ;; get detailed route info by verb and relative path
  (recognize-route :post "/api/users/1/login"))
