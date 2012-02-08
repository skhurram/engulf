(ns parbench.ning-client
   (:use lamina.core)
   (:require [clojure.tools.logging :as log])
   (:import com.ning.http.client.AsyncHttpClient
            com.ning.http.client.PerRequestConfig
            com.ning.http.client.AsyncCompletionHandler))

(defn client-handler [res-ch]
  (proxy [com.ning.http.client.AsyncCompletionHandler] []
          (onCompleted [response]
            (let [content-type   (.getContentType response)
                  status         (.getStatusCode  response)]
              (enqueue (.success res-ch)
                       {:status status :content-type content-type})))
          (onThrowable [e]
            (enqueue (.error res-ch) e))))

(def method-prep-map
     {:get (memfn prepareGet url)
      :put (memfn preparePut url)
      :post (memfn preparePost url)
      :delete (memfn prepareDelete url)})

(def id (atom 0))

(defn http-client [options]
  "Currently ignores all options"
  (let [client (AsyncHttpClient.)]
    (fn this
      ([request]
         (this request 90000))
      ([{:keys [method url]} timeout]
        (let [req-id (swap! id inc)
              result (result-channel)
              handler (client-handler result)
              requestConfig (doto (PerRequestConfig.)
                                  (.setRequestTimeoutInMs (int timeout)))]
          (-> ((get method-prep-map method) client url)
              (.setPerRequestConfig requestConfig)
              (.execute handler))
          result)))))
                  
(defn http-request
  "Aleph style interface"
  ([request]
     (http-request request -1))
  ([request timeout]
     (let [client (http-client {})]
       (client request timeout))))