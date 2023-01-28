(ns poly.web.rest-api.api
  (:require
   [clojure.set]
   [poly.web.rest-api.handler :as h]))

(def base-routes
  #{["/api/echo" :get h/echo :route-name :echo]})

(def user-routes
  #{["/api/users" :post h/user-register :route-name :user-register]
    ["/api/users/login/:id" :post h/user-login :route-name :user-login]})

(def routes (clojure.set/union base-routes
                               user-routes))
