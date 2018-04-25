(ns open-vpn-console.boundary.aws.ec2
  (:require [integrant.core :as ig]
            [amazonica.aws.ec2 :as ec2]))

(defprotocol IEC2
  (get-instances [this])
  (start-instance [this instance-id])
  (stop-instance [this instance-id]))

(defrecord EC2 [access-key secret-key endpoint]
  IEC2
  (get-instances [this]
    (->> (ec2/describe-instances {:access-key access-key
                                  :secret-key secret-key
                                  :endpoint endpoint})
         :reservations
         first
         :instances
         (map #(select-keys % [:instance-id :tags :state :public-ip-address]))))
  (start-instance [this instance-id]
    (-> (ec2/start-instances {:access-key access-key
                              :secret-key secret-key
                              :endpoint endpoint}
                             :instance-ids [instance-id])
        :starting-instances
        first
        (select-keys [:instance-id :current-state :code])
        (assoc :message "Instance is starting successfully.")))
  (stop-instance [this instance-id]
    (-> (ec2/stop-instances {:access-key access-key
                             :secret-key secret-key
                             :endpoint endpoint}
                            :instance-ids [instance-id])
        :stopping-instances
        first
        (select-keys [:instance-id :current-state :code])
        (assoc :message "Instance is stopping successfully."))))

(defmethod ig/init-key :open-vpn-console.boundary.aws/ec2 [_ {:keys [access-key
                                                                     secret-key
                                                                     endpoint]}]
  (map->EC2 {:access-key access-key
             :secret-key secret-key
             :endpoint endpoint}))
