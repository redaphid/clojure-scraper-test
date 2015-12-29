(ns scraper.core
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.java.io :as io])
    (:require [clj-http.client :as client])
    (:require [cemerick.url :refer (url url)])
    (:require [clojure.string :as string])
)

(defn download-images [site-url]
  (def folder (last (string/split site-url #"/")))
  (map (partial download-image folder) (get-image-links site-url))
)

(defn get-image-links [site-url]
  (def site-html (html/html-resource
    (java.io.StringReader. ((client/get site-url) :body))
  ))

   (def relative-links
     (map :src (map :attrs (html/select site-html [:center :a :img])))
   )
   (map (partial get-full-url site-url) relative-links)
)

(defn get-full-url [site-url relative-url]
  (str (url site-url relative-url))
)

(defn download-image [folder image-url]
  (def image-file (io/file "downloads" folder (last (string/split image-url #"/"))))
  (io/make-parents image-file)
  (io/copy
    (:body (client/get (str image-url) {:as :stream}))
    (io/file image-file)
  )
)
