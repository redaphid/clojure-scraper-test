(ns scraper.core
    (:require [net.cgrand.enlive-html :as html])
    (:require [clj-http.client :as client])
    (:require [cemerick.url :refer (url url)])
    (:require [clojure.string :as string])
)


(def site-html (html/html-resource
  (java.io.StringReader. ((client/get site-url) :body))
))

(defn get-image-links []
   (def relative-links
     (map :src (map :attrs (html/select site-html [:center :a :img])))
   )
   (map get-full-url relative-links)
)

(defn get-full-url [relative-url]
  (str (url site-url relative-url))
)

(defn download-image [image-url]
  (clojure.java.io/copy
    (:body (client/get (str image-url) {:as :stream}))
    (java.io.File. (str "downloads/" (last (string/split image-url #"/"))))
  )
)
