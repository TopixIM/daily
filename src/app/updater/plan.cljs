
(ns app.updater.plan
  (:require [app.schema :as schema]
            [bisection-key.util :refer [key-append key-before key-after]]))

(defn create [db op-data sid op-id op-time]
  (let [session (get-in db [:sessions sid]), user (get-in db [:users (:user-id session)])]
    (update-in
     db
     [:users (:user-id session) :plan]
     (fn [plan]
       (let [new-key (key-append plan)]
         (assoc plan new-key (merge schema/task {:id op-id, :time op-time, :text op-data})))))))

(defn move [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])
        from-id (:from op-data)
        to-id (:to op-data)]
    (update-in
     db
     [:users user-id :plan]
     (fn [plan]
       (let [new-key (if (< to-id from-id) (key-before plan to-id) (key-after plan to-id))]
         (-> plan (assoc new-key (get plan from-id)) (dissoc from-id)))))))

(defn remove-one [db op-data sid op-id op-time]
  (let [sort-id op-data, user-id (get-in db [:sessions sid :user-id])]
    (assoc-in db [:users user-id :plan sort-id :deleted?] true)))

(defn reuse [db op-data sid op-id op-time]
  (let [sort-id op-data, user-id (get-in db [:sessions sid :user-id])]
    (assoc-in db [:users user-id :plan sort-id :deleted?] false)))

(defn update-text [db op-data sid op-id op-time]
  (let [sort-id (:id op-data)
        text (:text op-data)
        user-id (get-in db [:sessions sid :user-id])]
    (update-in
     db
     [:users user-id :plan sort-id]
     (fn [task] (-> task (assoc :text text) (assoc :time op-time))))))
