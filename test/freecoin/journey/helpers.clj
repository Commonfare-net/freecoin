(ns freecoin.journey.helpers
  (:require [clojure.tools.logging :as log]
            [freecoin.blockchain :as blockchain]
            [freecoin.config :as c]
            [freecoin.db.storage :as s]
            [freecoin.journey.kerodon-checkers :as kc]
            [freecoin.routes :as routes]
            [freecoin.test-helpers.integration :as ih]
            [kerodon.core :as k]
            [midje.sweet :refer :all]
            [stonecutter-oauth.client :as soc]))

(def stores-m (s/create-mongo-stores (ih/get-test-db)))
(def blockchain (blockchain/new-stub (ih/get-test-db)))

(def test-app 
  (ih/build-app {:stores-m stores-m
                 :blockchain blockchain}))


(defn sign-up [state auth-code]
  (-> state
      (k/visit (str (routes/absolute-path (c/create-config) :sso-callback) "?code=" auth-code))
      (kc/check-and-follow-redirect "to account page")))

(def sign-in sign-up)

(defn sign-out [state]
  (k/visit state (routes/absolute-path (c/create-config) :sign-out)))


(defmacro create-user [auth-code sub email]
  (soc/request-access-token! anything auth-code)
  =>
  {:user-info {:sub sub
               :email email}})

(defn setup-users [users]
  (log/info "setup users" users)

  (map
   (fn [[code sub email]]
     (background
      (soc/request-access-token! anything code) => {:user-info {:sub sub
                                                                :email email}}
      )
     )
   users))


