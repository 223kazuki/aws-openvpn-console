(ns open-vpn-console.handler.api
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [open-vpn-console.boundary.aws.ec2 :as ec2]
            [selmer.parser :refer [render-file]]
            [me.raynes.fs :as fs]
            [me.raynes.fs.compression :refer [zip]]
            [clojure.string :as str]
            [ring.util.response :refer [file-response]]))

(defmethod ig/init-key ::login [_ {:keys [login-email login-password]}]
  (when-not (and login-email login-password)
    (throw (Exception. (str "Initialization error."
                            "You have to set LOGIN_EMAIL and LOGIN_PASSWORD as environment variables."))))
  (fn [{[_ params] :ataraxy/result}]
    (let [{:keys [email password]} params]
      (if (and (= email login-email)
               (= password login-password))
        {:status 200
         :session {:identity email}
         :body {:message "Login suceed."}}
        {:status 401
         :body {:message "Login failed."}}))))

(defmethod ig/init-key ::instances [_ {:keys [ec2]}]
  (fn [{[_] :ataraxy/result}]
    [::response/ok (ec2/get-instances ec2)]))

(defmethod ig/init-key ::operate-instance [_ {:keys [ec2]}]
  (fn [{[_ instance-id operation] :ataraxy/result}]
    (case operation
      "start" [::response/ok (ec2/start-instance ec2 instance-id)]
      "stop"  [::response/ok (ec2/stop-instance ec2 instance-id)]
      {:status 405
       :body {:message "Operation not allowed."}})))

(defmethod ig/init-key ::download-openvpn-file [_ {:keys [ec2]}]
  (fn [{[_ instance-id] :ataraxy/result}]
    (if-let [public-ip-address (some->> (ec2/get-instances ec2)
                                        (filter #(= (:instance-id %) instance-id))
                                        first
                                        :public-ip-address)]
      (let [output (io/file (fs/tmpdir) "openvpn.zip")
            resources
            (->> ["ca.crt" "client1.crt" "client1.key" "ta.key" "myvpn.ovpn.template"]
                 (map #(str "openvpn/" %))
                 (map io/resource)
                 (filter identity)
                 (map (fn [r]
                        (if (str/ends-with? (.getPath r) ".template")
                          (let [out (io/file (fs/tmpdir) (as-> (.getPath r) $
                                                           (str/split $ #"/")
                                                           (last $)
                                                           (str/split $ #"\.")
                                                           (butlast $)
                                                           (str/join "." $)))]
                            (spit out (render-file r {:public-ip-address public-ip-address}))
                            out) r)))
                 (map #(let [name (-> (.getPath %)
                                      (str/split #"/")
                                      last)
                             content (slurp %)]
                         [name content])))]
        (if (zero? (count resources))
          [::response/no-content]
          (do
            (zip output resources)
            (file-response (.getAbsolutePath output)))))
      [::response/no-content])))
