(ns brujug2016.core
  (:require [clojure.data.codec.base64 :as base64]
            [cemerick.url :as url]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as str]))

(def credentials {:consumer-key    "YOUR_KEY"
                  :consumer-secret "YOUR_SECRET"})

(defn string-to-base64-string [original]
  (String. (base64/encode (.getBytes original)) "UTF-8"))

(defn create-bearer-token-credentials
  [{:keys [consumer-key consumer-secret]}]
  (string-to-base64-string
    (str (url/url-encode consumer-key) ":" (url/url-encode consumer-secret))))

(defn bearer-token-request
  [twitter-config]
  {:method  :post
   :url     "https://api.twitter.com/oauth2/token"
   :headers {"Authorization" (str "Basic " (create-bearer-token-credentials twitter-config))
             "Content-Type"  "application/x-www-form-urlencoded;charset=UTF-8"}
   :body    "grant_type=client_credentials"})

(defn parse-response
  [response]
  (-> response
      :body
      (json/parse-string true)))

(defn get-access-token
  [credentials]
  (-> credentials
      (bearer-token-request)
      (http/request)
      (parse-response)
      :access_token))

(def access-token (get-access-token credentials))

(defn timeline
  [screen-name]
  {:method       :get
   :url          "https://api.twitter.com/1.1/statuses/user_timeline.json"
   :query-params {:screen_name screen-name
                  :count 200}})

(clojure.pprint/pprint (timeline "BruJUG"))

(http/request (timeline "BruJUG"))

(defn add-bearer-token
  [request token]
  (assoc-in
    request
    [:headers "Authorization"]
    (str "Bearer " token)))

(http/request
  (add-bearer-token
    (timeline "BruJUG") access-token))

(parse-response
  (http/request
    (add-bearer-token
      (timeline "BruJUG") access-token)))

;; kind of tedious, no? all code is backwards.

;; threading macro

(def tweets
  (-> "BruJUG"
      (timeline)
      (add-bearer-token access-token)
      (http/request)
      (parse-response)))

(count tweets)

(first tweets)

(-> tweets first :text)

(-> tweets (nth 2) :text)

(-> tweets (nth 2)
    :text
    (str/lower-case)
    (.contains "clojure"))

(defn about?
  [term]
  (fn [tweet]
    (-> tweet
        :text
        (str/lower-case)
        (.contains term))))

(count
  (filter
    (about? "clojure")
    tweets))

(map
  :created_at
  (filter
    (about? "clojure") tweets))

(->> tweets
     (filter (about? "clojure"))
     (map :retweet_count))

(->> tweets
     (filter (about? "clojure"))
     (map :retweet_count)
     (reduce +))

(->> tweets
     (filter (about? "java"))
     (map :retweet_count)
     (reduce +))


