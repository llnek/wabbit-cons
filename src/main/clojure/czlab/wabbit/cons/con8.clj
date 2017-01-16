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

  czlab.wabbit.cons.con8

  (:gen-class)

  (:require [czlab.wabbit.base.core :as bcc :refer :all]
            [czlab.xlib.io :refer [dirRead?]]
            [czlab.xlib.logging :as log]
            [clojure.java.io :as io]
            [czlab.table.core :as tbl])

  (:use [czlab.wabbit.cons.con2]
        [czlab.xlib.resources]
        [czlab.xlib.format]
        [czlab.xlib.core]
        [czlab.xlib.str]
        [czlab.xlib.consts]
        [czlab.wabbit.cons.con1])

  (:import [czlab.wabbit.cons CmdError]
           [czlab.xlib I18N]
           [java.io File]
           [java.util ResourceBundle List Locale]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- getCmdInfo
  ""
  [rcb]
  (partition
    2
    (rstr*
      rcb
      ["usage.new"] ["usage.new.desc"]
      ["usage.svc"] ["usage.svc.desc"]
      ["usage.podify"] ["usage.podify.desc"]
      ["usage.ide"] [ "usage.ide.desc"]
      ["usage.build"] [ "usage.build.desc"]
      ["usage.test"] [ "usage.test.desc"]

      ["usage.debug"] ["usage.debug.desc"]
      ["usage.start"] ["usage.start.desc"]

      ["usage.gen"] [ "usage.gen.desc"]
      ["usage.demo"] [ "usage.demo.desc"]
      ["usage.version"] [ "usage.version.desc"]

      ["usage.testjce"] ["usage.testjce.desc"]
      ["usage.help"] ["usage.help.desc"])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn usage
  ""
  []
  (let
    [walls ["" "   " ""]
     style {:middle ["" "" ""]
            :bottom ["" "" ""]
            :top ["" "" ""]
            :dash " "
            :header-walls walls
            :body-walls walls}
     rcb (I18N/base)]
    (println (bannerText))
    (printf "%s\n\n" (rstr rcb "wabbit.desc"))
    (printf "%s\n" (rstr rcb "cmds.header"))
    ;; prepend blanks to act as headers
    (printf "%s\n\n"
            (strim
              (with-out-str
                (-> (concat '(("" ""))
                            (getCmdInfo rcb))
                    (tbl/table :style style)))))
    (printf "%s\n" (rstr rcb "cmds.trailer"))
    (println)
    ;;the table module causes some agent stuff to hang
    ;;the vm without exiting, so shut them down
    (shutdown-agents)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn -main
  "Main Entry"
  [& args]
  (let
    [ver (loadResource bcc/c-verprops)
     rcb (getResource bcc/c-rcb)
     verStr (or (some-> ver (.getString "version")) "?")]
    (sysProp! "wabbit.version" verStr)
    (I18N/setBase rcb)
    (try
      (if (empty? args)(trap! CmdError))
      (let [[f _]
            (-> (keyword (first args))
                (*wabbit-tasks* ))]
        (if (fn? f)
          (f (vec (drop 1 args)))
          (trap! CmdError)))
      (catch Throwable _
        (if (inst? CmdError _) (usage) (prtStk _))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


