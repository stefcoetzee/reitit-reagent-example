(ns app.main
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdc]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rcs]
            [spec-tools.data-spec :as ds]
            [fipp.edn :as fedn]))

(defn homepage
  []
  [:div
   [:h1 "Homepage"]
   [:button
    {:type "button"
     :on-click (fn [] (rfe/push-state ::item {:id 3}))}
    "Push state (push entry onto history stack): item = 3"]
   [:button
    {:type "button"
     :on-click (fn [] (rfe/replace-state ::item {:id 4}))}
    "Replace state (mutate latest entry on history stack): item = 4"]])

(defn about-page
  []
  [:div
   [:h1 "About"]
   [:ul
    [:li [:a {:href "https://duckduckgo.com"} "External link to DuckDuckGo"]]
    #_[:li [:a {:href (rfe/href ::foobar)} "Missing route (see console for message)"]]
    #_[:li [:a {:href (rfe/href ::item)} "Missing route params (see console for message)"]]]
   [:div
    {:content-editable true
     :suppressContentEditableWarning true}
    [:p "Link inside contentEditable element is ignored"]
    [:a {:href (rfe/href ::homepage)} "Link to home (ignored)"]]])

(defn item-page
  [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path]
    [:div
     [:h2 "Select item " id]
     (when (:foo query)
       [:p "Optional `foo` query param: " (:foo query)])]))

(defonce match (r/atom nil))

(defonce root (r/atom (rdc/create-root (.getElementById js/document
                                                        "app"))))

(defn current-page
  []
  [:div
   [:ul
    [:li [:a {:href (rfe/href ::homepage)} "Home"]]
    [:li [:a {:href (rfe/href ::about)} "About"]]]
   [:p "Header"]
   (when @match
     (let [view (:view (:data @match))]
       (js/console.log @match)
       [view @match]))
   [:pre (with-out-str (fedn/pprint @match))]])

(def routes
  [["/"
    {:name ::homepage
     :view #'homepage}]

   ["/about"
    {:name ::about
     :view #'about-page}]

   ["/item/:id"
    {:name ::item
     :view #'item-page
     :parameters {:path {:id int?}
                  :query {(ds/opt :foo) keyword?}}}]])

(def functional-compiler (r/create-compiler {:function-components true}))

(defn ^:dev/after-load start
  []
  (js/console.log "Start")
  (rdc/render @root
              [current-page]
              functional-compiler))

(defn init
  []
  (js/console.log "Initialize")
  (rfe/start! (rf/router routes {:data {:coercion rcs/coercion}})
              (fn [m]
                (reset! match m))
              {:use-fragment false})

  (start))

(defn ^:dev/before-load stop
  []
  (js/console.log "Stop"))

(comment
  (fedn/pprint @match))
