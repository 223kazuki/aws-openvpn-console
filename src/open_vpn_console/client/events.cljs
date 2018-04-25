(ns open-vpn-console.client.events
  (:require [re-frame.core :as re-frame]
            [open-vpn-console.client.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [ajax.protocols :as protocol]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::hide-fixed-menu
 (fn [db _]
   (assoc db :menu-fixed? false)))

(re-frame/reg-event-db
 ::show-fixed-menu
 (fn [db _]
   (assoc db :menu-fixed? true)))

(re-frame/reg-event-db
 ::set-active-panel
 (fn  [db [_ active-panel]]
   (-> db
       (assoc :active-panel active-panel))))

(re-frame/reg-event-fx
 ::fetch-instances
 (fn [{:keys [db]} _]
   {:db   (assoc db :loading? true)
    :http-xhrio {:method          :get
                 :uri             "/api/instances"
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [::fetch-instances-success]
                 :on-failure      [::fetch-instances-failure]}}))

(re-frame/reg-event-db
 ::fetch-instances-success
 (fn [db [_ result]]
   (assoc db
          :loading? false
          :instances result)))

(re-frame/reg-event-db
 ::fetch-instances-failure
 (fn [db [_ result]]
   (js/console.log (:status result))
   (if (== (:status result) 401)
     (aset js/window.location "href" "/login"))))

(re-frame/reg-event-fx
 ::start-instance
 (fn [{:keys [db]} [_ instance-id]]
   {:db   (assoc db :loading? true)
    :http-xhrio {:method          :post
                 :uri             (str "/api/instances/" instance-id "/start")
                 :timeout         8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :params          nil
                 :format          (ajax/json-request-format)
                 :on-success      [::operate-instance-success]
                 :on-failure      [::operate-instance-failure]}}))

(re-frame/reg-event-fx
 ::stop-instance
 (fn [{:keys [db]} [_ instance-id]]
   {:db   (assoc db :loading? true :instance-operation-result nil)
    :http-xhrio {:method          :post
                 :uri             (str "/api/instances/" instance-id "/stop")
                 :timeout         8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :params          nil
                 :format          (ajax/json-request-format)
                 :on-success      [::operate-instance-success]
                 :on-failure      [::operate-instance-failure]}}))

(re-frame/reg-event-db
 ::operate-instance-success
 (fn [db [_ result]]
   (assoc db
          :loading? false
          :instance-operation-result {:type :success :message (:message result)})))

(re-frame/reg-event-db
 ::operate-instance-failure
 (fn [db [_ result]]
   (if (== (:status result) 401)
     (aset js/window.location "href" "/login")
     (assoc db
            :loading? false
            :instance-operation-result {:type :failure :message (:message result)}))))

(re-frame/reg-event-fx
 ::download-openvpn-file
 (fn [{:keys [db]} [_ instance-id]]
   {:db   (assoc db :loading? true :download-openvpn-file-result nil)
    :http-xhrio {:method          :post
                 :uri             (str "/api/instances/" instance-id "/openvpn/download")
                 :timeout         8000
                 :response-format {:content-type "application/zip"
                                   :description "zip"
                                   :read protocol/-body
                                   :type :arraybuffer}
                 :params          nil
                 :format          (ajax/json-request-format)
                 :on-success      [::download-openvpn-file-success]
                 :on-failure      [::download-openvpn-file-failure]}}))

(re-frame/reg-event-db
 ::download-openvpn-file-success
 (fn [db [_ response]]
   (let [file (js/Blob. (clj->js [response])
                        (clj->js {:type "application/zip"}))]
     (.open js/window (.createObjectURL js/URL file) "_blank"))
   (assoc db :loading? false)))

(re-frame/reg-event-db
 ::download-openvpn-file-failure
 (fn [db [_ result]]
   (if (== (:status result) 401)
     (aset js/window.location "href" "/login")
     (assoc db
            :loading? false
            :download-openvpn-file-result {:type :failure :message (:message result)}))))
