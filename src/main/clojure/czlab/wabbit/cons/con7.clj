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

  (:require [czlab.wabbit.base :as bcc :refer :all]
            [czlab.basal.io :refer [dirRead?]]
            [czlab.basal.logging :as log]
            [clojure.java.io :as io]
            [io.aviso.ansi :as ansi]
            [czlab.table.core :as tbl])

  (:use [czlab.wabbit.cons.con2]
        [czlab.basal.resources]
        [czlab.basal.format]
        [czlab.basal.core]
        [czlab.basal.str]
        [czlab.wabbit.cons.con1])

  (:import [czlab.jasal DataError I18N]
           [java.io File]
           [java.util ResourceBundle List Locale]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- getCmdInfo "" [rcb]

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
(defn usage "" []

  (let
    [walls ["" "   " ""]
     style {:middle ["" "" ""]
            :bottom ["" "" ""]
            :top ["" "" ""]
            :dash " "
            :header-walls walls
            :body-walls walls}
     rcb (I18N/base)]
    (prn!! (ansi/bold-yellow (bannerText)))
    (prn! "%s\n\n" (rstr rcb "wabbit.desc"))
    (prn! "%s\n" (rstr rcb "cmds.header"))
    ;; prepend blanks to act as headers
    (prn! "%s\n\n"
          (strim
            (with-out-str
              (-> (concat '(("" ""))
                          (getCmdInfo rcb))
                  (tbl/table :style style)))))
    (prn! "%s\n" (rstr rcb "cmds.trailer"))
    (prn!! "")
    ;;the table module causes some agent stuff to hang
    ;;the vm without exiting, so shut them down
    (shutdown-agents)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn -main "" [& args]

  (let [ver (loadResource bcc/c-verprops)
        rcb (getResource bcc/c-rcb)
        verStr (or (some-> ver (.getString "version")) "?")]
    (sysProp! "wabbit.version" verStr)
    (I18N/setBase rcb)
    (try
      (if (empty? args)(throwBadData "CmdError"))
      (let [[f _]
            (-> (keyword (first args))
                *wabbit-tasks* )]
        (if (fn? f)
          (f (vec (drop 1 args)))
          (throwBadData "CmdError")))
      (catch Throwable _
        (if (ist? DataError _) (usage) (prtStk _))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


