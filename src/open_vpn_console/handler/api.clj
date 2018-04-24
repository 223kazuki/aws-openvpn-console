(ns open-vpn-console.handler.api
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [open-vpn-console.boundary.aws.ec2 :as ec2]))

(defmethod ig/init-key ::login [_ options]
  (fn [{[_ params] :ataraxy/result}]
    (let [{:keys [email password]} params]
      (if (and (= email "test@example.com")
               (= password "password"))
        {:status 200
         :session {:identity email}
         :body {:message "Login suceed."}}
        {:status 401
         :body {:message "Login failed."}}))))

(defmethod ig/init-key ::instances [_ {:keys [ec2]}]
  (fn [{[_] :ataraxy/result}]
    [::response/ok (ec2/get-instances ec2)]))
