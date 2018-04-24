(ns open-vpn-console.client
  (:require [open-vpn-console.client.login :as login]
            [open-vpn-console.client.core :as core]
            [goog.events :as events]))

;;(if (= js/location.pathname "/login")
;;(events/listen js/window "load" #(when-not @login/system (login/start)))
;;(events/listen js/window "load" #(when-not @core/system (core/start))))

(if (= js/location.pathname "/login")
  (if-not @login/system
    (login/start)
    (do (login/stop) (login/start)))
  (if-not @core/system
    (core/start)
    (do (core/stop) (core/start))))
