(ns ewen.cle-usb.client-core
  (:require [clojure.browser.repl] ;Only for development mode. TODO find a way to make a conditional require
            [sablono.core :as html :refer-macros [html html-expand]]
            [ewen.dragdrop :as dd]
            [cljs.core.async :as async]
            [ewen.async-plus :as async+]
            [goog.style :as gstyle]
            [ewen.cle-usb.render :as render]
            [ewen.cle-usb.client :as client]
            [datascript :as ds]
            [ewen.cle-usb.data :as data])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [ewen.async-plus.macros :as async+m]))


(defn init []
  (enable-console-print!)
  (js/React.initializeTouchEvents true))







(def app (data/load-app))



(init)



;client
(client/request-render app)


;render
(defn render-view [render-data load-chan]
  (render/request-render (:view render-data)
                         load-chan
                         (:data render-data)))




;Handle menu events
(defmulti handle-menu-event identity)





(defn maybe-update-pos [pwd-map id pos]
  (if (= id (:id pwd-map)) (assoc pwd-map :position pos) pwd-map))

;Passwords position
(async+m/go-loop [pos-ch (data/get-pwd-pos-chan @app)]
                 (when-let [{:keys [id pos]} (async/<! pos-ch)]
                   (let [render-data (data/get-render-data @app)
                         updated-data  (-> (map #(maybe-update-pos % id pos)
                                                 (:data render-data))
                                           vec)
                         render-data (assoc render-data :data updated-data)]
                     (render-view render-data (data/view-load-channel @app)))
                   (recur pos-ch)))



(data/listen-for! app
                  :state/dragging
                  ::dragging-listener
                  #(render-view (data/get-render-data @app)
                                (data/view-load-channel @app)))

(data/listen-for! app
                  :state/position
                  ::position-listener
                  #(render-view (data/get-render-data @app)
                                (data/view-load-channel @app)))

(data/listen-for! app
                  :view/current
                  ::menu-events-listener
                  #(render-view (data/get-render-data @app)
                                (data/view-load-channel @app)))

(data/listen-for! app
                  :state/sort-index
                  ::sort-index-listener
                  #(render-view (data/get-render-data @app)
                                (data/view-load-channel @app)))





(render-view (data/get-render-data @app)
             (data/view-load-channel @app))








(comment

  (load-app)

  (let [schema {:aka {:cardinality :many}}
        conn   (ds/create-conn)
        param "rr"]
    (ds/transact! conn [ { :db/id -1
                          :name  param
                          :age   45
                          :aka   ["Maks Otto von Stirlitz" "Jack Ryan"] } ])
    (ds/q '[ :find  ?n ?a
            :where [?e :aka ["Maks Otto von Stirlitz" "Jack Ryan"]]
            [?e :name ?n]
            [?e :age  ?a] ]
         @conn))








  (def ddd (load-app))
  (get (:tempids (ds/transact! ddd [
                                     {:db/id          -1
                                      :password/label "Password5"}
                                     #_[:db/retract 6 :password/label "Password5"]
                                     ])) -1)

  (ds/q '[:find ?id
          :where [?id :password/label _]]
        @ddd)


  (ds/entity @ddd (only #{[7]}))




  (ds/listen! ddd :ll #(.log js/console (str (:tx-data %))))
  (ds/unlisten! ddd :ll)



  (ds/q '[:find ?id ?name
          :where [?id :react/name ?name]]
        @app)







  (data/get-channels @app)

  )

