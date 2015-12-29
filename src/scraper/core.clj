(ns scraper.core
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.java.io :as io])
    (:require [clj-http.client :as client])
    (:require [cemerick.url :refer (url url)])
    (:require [clojure.string :as string])
    (:require [clj-time.core :as time])
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
     (map :href (map :attrs (html/select site-html [(html/attr-starts :href "attachment.php")])))
   )
   (map (partial get-full-url site-url) relative-links)
)

(defn get-full-url [site-url relative-url]
  (str (assoc (url site-url) :path (str "/" relative-url)))
)

(defn download-image [folder image-url]
  (def image-file (io/file "downloads" folder (str (time/now) ".jpg")))
  (io/make-parents image-file)
  (io/copy
    (:body (client/get (str image-url) {:as :stream, :cookies cookie}))
    (io/file image-file)
  )
)


(defn parse-cookie [raw-cookie]
  (into {}
    (map
      (fn [cookie-kv]
        (def cookie-pair (clojure.string/split cookie-kv #"="))
        [
          (first cookie-pair)
          {:discard false, :path "/", :value (second cookie-pair), :version 1}
        ]
      )
      (clojure.string/split raw-cookie #"; ")
    )
  )
)

; (def cookie (parse-cookie raw-cookie))
; (def image (client/get image-url {:cookies cookie, :as :stream}))
