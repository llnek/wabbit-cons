;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.cons.con2

  (:require [czlab.wabbit.shared.new :as ws]
            [czlab.basal.logging :as log]
            [stencil.core :as sc]
            [clojure.string :as cs]
            [clojure.java.io :as io])

  (:use [czlab.wabbit.base.core]
        [czlab.basal.core]
        [czlab.basal.io]
        [czlab.basal.str])

  (:import [czlab.wabbit.cons CmdError]
           [java.io File]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;simulate what lein new template does
(defn createPod "" [name & args]

  (prn!! "Generating fresh 'wabbit' project.")
  (try
    (let
      [dir (second (drop-while #(not= "--to-dir" %) args))
       options
       {:renderer-fn sc/render-string
        :force?
        (some? (first (drop-while
                        #(not= "--force" %) args)))
        :dir (or dir
                 (-> (getCwd)
                     (io/file name) .getPath))}]
      ;;(prn!! "opts = " options)
      (apply ws/new<> name options args))
    (catch Throwable t
      (prn!! "failed to generate project")
      (prn!! (.getMessage t)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn publishSamples "" [outDir] )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


