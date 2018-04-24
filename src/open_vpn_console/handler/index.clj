(ns open-vpn-console.handler.index
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [clojure.java.io :as io]
            [integrant.core :as ig]))

(defmethod ig/init-key :open-vpn-console.handler/login [_ options]
  (fn [{[_] :ataraxy/result}]
    [::response/ok (slurp (io/resource "open_vpn_console/public/login.html"))]))

(defmethod ig/init-key :open-vpn-console.handler/logout [_ options]
  (fn [{[_] :ataraxy/result}]
    [::response/found "/login"]))

(defmethod ig/init-key :open-vpn-console.handler/index [_ options]
  (fn [{[_] :ataraxy/result}]
    [::response/ok (slurp (io/resource "open_vpn_console/public/index.html"))]))
