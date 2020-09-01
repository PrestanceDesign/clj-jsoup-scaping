(ns jsoup-scraping.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import org.jsoup.Jsoup))

(def URL "http://www.newegg.com/Product/ProductList.aspx?Submit=ENE&N=-1&IsNodeId=1&Description=GTX&bop=And&Page=1&PageSize=36&order=BESTMATCH")

(defn get-page []
  (.get (Jsoup/connect URL)))

(defn get-elems [page css]
  (.select page css))

(defn -main []
  (let [html (get-page)
        elems (get-elems html "div.item-container")
        headers ["brand" "product_name" "shipping"]]
    (with-open [writer (io/writer "graphics_cards.csv")]
      (csv/write-csv writer
                     (cons headers
                           (for [e elems
                                 :let [product-name (str/replace (.text (get-elems e ".item-title")) "," "|")
                                       brand (.attr (get-elems e "div.item-branding > a > img") "title")
                                       shipping (-> (get-elems e "div.item-action ul.price > li.price-ship")
                                                    .text
                                                    (str/replace "$" "")
                                                    (str/replace "Shipping" ""))]]
                             (conj (vector brand) product-name shipping)))))))
