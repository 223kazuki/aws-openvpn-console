(ns open-vpn-console.client.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::menu-fixed?
 (fn [db]
   (:menu-fixed? db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db]
   (:active-panel db)))

(re-frame/reg-sub
 ::instances
 (fn [db]
   (:instances db)))

(re-frame/reg-sub
 ::instance-operation-result
 (fn [db]
   (:instance-operation-result db)))

(re-frame/reg-sub
 ::download-openvpn-file-result
 (fn [db]
   (:download-openvpn-file-result db)))
