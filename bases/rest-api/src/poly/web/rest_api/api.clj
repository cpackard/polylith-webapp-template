(ns poly.web.rest-api.api
  (:require
   [clojure.set]
   [poly.web.rest-api.handler :as h]
   [poly.web.rest-api.middleware :as m]))

(def auth-intcs [m/wrap-auth-user m/wrap-authorization])

(def numeric #"[0-9]+")
(def url-rules {:user-id numeric})

(def base-routes
  #{["/api/echo" :get `h/echo :route-name :echo]})

(def user-routes
  #{["/api/users"
     :post `h/user-register
     :route-name :user-register]
    ["/api/users/:user-id"
     :get (conj auth-intcs `h/user-info)
     :route-name :user-info
     :constraints url-rules]
    ["/api/users/:user-id/login"
     :post `h/user-login
     :route-name :user-login
     :constraints url-rules]})

(def routes (clojure.set/union base-routes
                               user-routes))
