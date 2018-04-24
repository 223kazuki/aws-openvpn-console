(ns open-vpn-console.client.events
  (:require [re-frame.core :as re-frame]
            [open-vpn-console.client.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

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
   {:db   (assoc db :show-twirly true)
    :http-xhrio {:method          :get
                 :uri             "/api/instances"
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [::good-http-result]
                 :on-failure      [::bad-http-result]}}))

(re-frame/reg-event-db
 ::good-http-result
 (fn [db [_ result]]
   (assoc db :instances result)))

(re-frame/reg-event-db
 ::bad-http-result
 (fn [db [_ result]]
   (js/console.log (:status result))
   (if (== (:status result) 401)
     (aset js/window.location "href" "/login"))))
