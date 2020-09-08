(ns jsoup-scraping.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import org.jsoup.Jsoup))

(def site-url "http://www.newegg.com/Product/ProductList.aspx?Submit=ENE&N=-1&IsNodeId=1&Description=GTX&bop=And&Page=1&PageSize=36&order=BESTMATCH")

(defn fetch-page [url]
  (.get (Jsoup/connect url)))

(defn get-text [root selector]
  (-> (.select root selector)
      .text))

(defn get-attr [root selector attribute]
  (-> (.select root selector)
      (.attr attribute)))

(defn parse-product [item]
  (let [product-name (-> (get-text item ".item-title")
                         (str/replace "," "|"))
        brand        (get-attr item "div.item-branding > a > img" "title")
        shipping     (-> (.text (.select item "div.item-action ul.price > li.price-ship"))
                         (str/replace "$" "")
                         (str/replace "Shipping" ""))]
    [brand product-name shipping]))

(defn generate-csv [filename url]
  (let [html (fetch-page url)
        csv-rows (map parse-product (.select html "div.item-container"))
        headers ["brand" "product_name" "shipping"]]
    (with-open [writer (io/writer filename)]
      (csv/write-csv writer
                     (concat [headers]
                             csv-rows)))))

(defn -main []
  (generate-csv "graphics_cards.csv" site-url))

(comment
  (def html (fetch-page site-url))

  (def items (.select html "div.item-container"))

  (.select (first items) ".item-title")

  (.attr (.select (first items) ".item-branding > a > img") "title")

  (parse-product (first items))

  (generate-csv "graphics_cards.csv" site-url))
