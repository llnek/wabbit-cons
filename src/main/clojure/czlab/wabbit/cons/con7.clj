;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.cons.con7

  (:gen-class)

  (:require [czlab.basal.io :as i :refer [dirRead?]]
            [czlab.wabbit.base :as b]
            [czlab.basal.log :as log]
            [clojure.java.io :as io]
            [io.aviso.ansi :as ansi]
            [czlab.table.core :as tbl]
            [czlab.basal.resources :as r]
            [czlab.basal.format :as f]
            [czlab.basal.core :as c]
            [czlab.basal.str :as s]
            [czlab.wabbit.cons.con2 :as c2]
            [czlab.wabbit.cons.con1 :as c1])

  (:import [czlab.jasal DataError I18N]
           [java.io File]
           [java.util ResourceBundle List Locale]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- getCmdInfo "" [rcb pod?]

  (let [arr '(["usage.gen"] [ "usage.gen.desc"]
              ["usage.version"] [ "usage.version.desc"]
              ["usage.testjce"] ["usage.testjce.desc"]
              ["usage.help"] ["usage.help.desc"])
        arr (if pod?
              (concat '(["usage.debug"] ["usage.debug.desc"]
                        ["usage.start"] ["usage.start.desc"]
                        ["usage.stop"] ["usage.stop.desc"]) arr) arr)]
    (partition 2 (apply r/rstr* rcb arr))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- usage "" [pod?]

  (let
    [walls ["" "        " ""]
     style {:middle ["" "" ""]
            :bottom ["" "" ""]
            :top ["" "" ""]
            :dash " "
            :header-walls walls
            :body-walls walls}
     rcb (I18N/base)]
    (c/prn!! (ansi/bold-yellow (b/bannerText)))
    (c/prn! "%s\n\n" (r/rstr rcb "wabbit.desc"))
    (c/prn! "%s\n" (r/rstr rcb "cmds.header"))
    ;; prepend blanks to act as headers
    (c/prn! "%s\n\n"
            (s/strim
              (with-out-str
                (-> (concat '(("" ""))
                            (getCmdInfo rcb pod?))
                    (tbl/table :style style)))))
    (c/prn! "%s\n" (r/rstr rcb "cmds.trailer"))
    (c/prn!! "")
    ;;the table module causes some agent stuff to hang
    ;;the vm without exiting, so shut them down
    (shutdown-agents)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn -main "" [& args]
  (let [ver (r/loadResource b/c-verprops)
        rcb (r/getResource b/c-rcb)
        [p1 p2 & _] args
        pod? (= "-domus" p1)
        verStr (or (some-> ver (.getString "version")) "?")]
    (c/sysProp! "wabbit.version" verStr)
    (I18N/setBase rcb)
    (try
      (if (empty? args)(c/throwBadData "CmdError"))
      (let [pred #(and (or (= "-home" %1)
                           pod?) (s/hgl? %2))
            args (if (pred p1 p2) (drop 2 args) args)]
        (->> (if (pred p1 p2) p2 (c/getCwd))
             io/file
             c/fpath
             (c/sysProp! "wabbit.user.dir"))
        (let [cfg (b/slurpXXXConf (c1/getHomeDir) b/cfg-pod-cf)
              [f _] (-> (keyword (first args)) c1/*wabbit-tasks* )]
          (if (fn? f)
            (binding [c1/*config-object* cfg
                      c1/*pkey-object* (-> (get-in cfg
                                                   [:info :digest])
                                           c/charsit )]
              (f (vec (drop 1 args))))
            (c/throwBadData "CmdError"))))
      (catch Throwable _
        (if (c/ist? DataError _) (usage pod?) (c/prtStk _))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


