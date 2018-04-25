(ns open-vpn-console.client.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [open-vpn-console.client.subs :as subs]
            [open-vpn-console.client.events :as events]
            [soda-ash.core :as sa]
            [cljsjs.semantic-ui-react]
            [cljs.core.async :refer [timeout <!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn instances-panel []
  (let [instances (re-frame/subscribe [::subs/instances])
        result (re-frame/subscribe [::subs/instance-operation-result])]
    [:div
     (when-let  [{:keys [type message]} @result]
       (if (= type :failure)
         [sa/Message {:as "h3" :color "red" :error true} message]
         [sa/Message {:as "h3" :color "blue"} message]))
     [sa/Table {:celled true :style {:width "100%"}}
      [sa/TableHeader
       [sa/TableRow
        [sa/TableHeaderCell {:width 4} "ID"]
        [sa/TableHeaderCell {:width 1} "Status"]
        [sa/TableHeaderCell {:width 3} "IP Address"]
        [sa/TableHeaderCell ""]]]
      [sa/TableBody
       (for [{:keys [instance-id state public-ip-address]} @instances]
         [sa/TableRow {:key instance-id}
          [sa/TableCell instance-id]
          [sa/TableCell (:name state)]
          [sa/TableCell public-ip-address]
          [sa/TableCell
           (if (= (:name state) "stopped")
             [sa/Button {:primary true
                         :onClick #(re-frame/dispatch [::events/start-instance instance-id])}
              "起動"]
             (if (= (:name state) "running")
               [:div
                [sa/Button {:primary true
                            :onClick #(re-frame/dispatch [::events/stop-instance instance-id])}
                 "停止"]
                [sa/Button {:primary true
                            :onClick #(re-frame/dispatch [::events/download-openvpn-file instance-id])}
                 "設定ファイルダウンロード"]]))]])]]]))

(defn about-panel []
  [:div "About"])

(defn none-panel []
  [:div])

(defmulti panels identity)
(defmethod panels :instances-panel [] #'instances-panel)
(defmethod panels :about-panel [] #'about-panel)
(defmethod panels :none [] #'none-panel)
(defmethod panels :default [] [:div "This page does not exist."])

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(defn main-panel []
  (reagent/create-class
   {:component-did-mount
    #(do (re-frame/dispatch [::events/fetch-instances])
         (go-loop []
           (<! (timeout 5000))
           (re-frame/dispatch [::events/fetch-instances])
           (recur)))

    :reagent-render
    (fn []
      (let [active-panel (re-frame/subscribe [::subs/active-panel])
            instances    (re-frame/subscribe [::subs/instances])]
        (if (nil? @instances)
          [:div {:style {:height "100%" :pointer-events "none"}}
           [sa/Dimmer {:active true}
            [sa/Loader "Loading"] ]]
          [:div
           [sa/Menu {:fixed "top" :inverted true}
            [sa/Container
             [sa/MenuItem {:as "a" :header true  :href "/"}
              "Opne VPN Console"]
             [sa/MenuItem {:as "a" :href "/about"} "使い方"]
             [sa/MenuItem {:position "right"}
              [sa/Button {:as "a" :href "/logout" :inverted true} "Logout"]]]]
           [sa/Container {:style {:marginTop "7em"}}
            [css-transition-group {:transition-name "pageChange"
                                   :transition-enter-timeout 500
                                   :transition-leave-timeout 500
                                   :component "div"
                                   :className "transition"}
             ^{:key @active-panel}
             [(panels @active-panel)]]]])))}))
