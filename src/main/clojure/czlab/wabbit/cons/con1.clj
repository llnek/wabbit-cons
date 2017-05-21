;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.cons.con1

  (:require [czlab.basal.format :as f :refer [writeEdnStr readEdn]]
            [czlab.wabbit.core :as wc :refer [startViaCons]]
            [czlab.twisty.core :as tc :refer [assertJce]]
            [czlab.basal.resources :as r :refer [rstr]]
            [czlab.twisty.codec :as co]
            [czlab.basal.log :as log]
            [czlab.antclj.antlib :as a]
            [clojure.java.io :as io]
            [io.aviso.ansi :as ansi]
            [clojure.string :as cs]
            [czlab.wabbit.base :as b]
            [czlab.basal.guids :as g]
            [czlab.basal.core :as c]
            [czlab.basal.str :as s]
            [czlab.basal.io :as i]
            [czlab.basal.meta :as m]
            [czlab.wabbit.cons.con2 :as c2])

  (:import [org.apache.commons.io FileUtils]
           [czlab.basal Cljrt]
           [java.util
            ResourceBundle
            Properties
            Calendar
            Map
            Date]
           [java.io File]
           [czlab.jasal I18N]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;dummy
(defn- getHomeDir [])
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn bannerText
  "" {:no-doc true
      :tag String} []
  (str  "              __   __   _ __ " "\n"
        " _    _____ _/ /  / /  (_) /_" "\n"
        "| |/|/ / _ `/ _ \\/ _ \\/ / __/" "\n"
        "|__,__/\\_,_/_.__/_.__/_/\\__/ " "\n"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelpXXX "" [pfx end]
  (let [rcb (I18N/base)]
    (dotimes [n end]
      (c/prn! "%s\n" (r/rstr rcb (str pfx (inc n))))) (println)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Create "" [] (onHelpXXX "usage.new.d" 5))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onCreate
  "Create a new pod"
  {:no-doc true} [args]
  (if (not-empty args)
    (apply c2/createPod
           (args 0)
           (drop 1 args)) (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Podify "" [] (onHelpXXX "usage.podify.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- bundlePod "" [podDir outDir]

  (let [dir (i/mkdirs (io/file outDir))
        a (io/file podDir)]
    (a/run*
      (a/zip
        {:destFile (io/file dir (str (.getName a) ".zip"))
         :basedir a
         :includes "**/*"}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onPodify
  "" {:no-doc true} [args]

  (if-not (empty? args)
    (bundlePod (b/getProcDir) (args 0))
    (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Start "" [] (onHelpXXX "usage.start.d" 4))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- runPodBg "" [podDir]

  (let
    [progW (io/file podDir "bin/wabbit.bat")
     prog (io/file podDir "bin/wabbit")
     tk (if (c/isWindows?)
          (a/exec
            {:executable "cmd.exe"
             :dir podDir}
            [[:argvalues ["/C" "start" "/B"
                          "/MIN"
                          (c/fpath progW) "run"]]]))
     _ (if false
          (a/exec
            {:executable (c/fpath prog)
             :dir podDir}
            [[:argvalues ["run" "bg"]]]))]
    (if tk
      (a/run* tk) (c/throwBadData "CmdError"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onStart
  "" {:no-doc true} [args]

  (let [cwd (b/getProcDir)
        s2 (first args)]
    ;; background job is handled differently on windows
    (if (and (c/in? #{"-bg" "--background"} s2)
             (c/isWindows?))
      (runPodBg cwd)
      (do
        (c/prn!! (ansi/bold-yellow (bannerText)))
        (wc/startViaCons cwd)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Debug "" [] (onHelpXXX "usage.debug.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onDebug "Debug the pod" {:no-doc true} [args] (onStart args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Demos "" [] (onHelpXXX "usage.demo.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onDemos
  "" {:no-doc true} [args]

  (if-not (empty? args)
    (c2/publishSamples (args 0)) (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn genPwd "" [args]

  (let [c (first args)
        n (c/convLong (str c) 16)]
    (if (and (>= n 8)
             (<= n 48))
      (-> (co/strongPwd<> n)
          co/p-text c/strit)
      (c/throwBadData "CmdError"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn genWwid "" [] (g/wwid<>))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn genGuid "" [] (c/uuid<>))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onEncrypt "" [args]

  (if (> (count args) 1)
    (->> (co/pwd<> (args 1)
                   (args 0)) co/p-encoded c/strit )
    (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onDecrypt "" [args]

  (if (> (count args) 1)
    (->> (co/pwd<> (args 1)
                   (args 0)) co/p-text c/strit )
    (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHash "" [args]

  (if-not (empty? args)
    (-> (co/hashed (co/pwd<> (first args))))
    (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Generate "" [] (onHelpXXX "usage.gen.d" 8))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onGenerate
  "" {:no-doc true} [args]

  (let [c (first args)
        args (vec (drop 1 args))]
    (cond
      (c/in? #{"-p" "--password"} c)
      (genPwd args)
      (c/in? #{"-h" "--hash"} c)
      (onHash args)
      (c/in? #{"-u" "--uuid"} c)
      (genGuid)
      (c/in? #{"-w" "--wwid"} c)
      (genWwid)
      (c/in? #{"-e" "--encrypt"} c)
      (onEncrypt args)
      (c/in? #{"-d" "--decrypt"} c)
      (onDecrypt args)
      :else (c/throwBadData "CmdError"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn prnGenerate
  "" {:no-doc true} [args] (c/prn!! (onGenerate args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-TestJCE "" [] (onHelpXXX "usage.testjce.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onTestJCE
  "" {:no-doc true} [args]

  (let [rcb (I18N/base)]
    (tc/assertJce)
    (c/prn!! (r/rstr rcb "usage.testjce.ok"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Version "" [] (onHelpXXX "usage.version.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onVersion
  "" {:no-doc true} [args]

  (let [rcb (I18N/base)]
    (->> (c/sysProp "wabbit.version")
         (r/rstr rcb "usage.version.o1")
         (c/prn! "%s\n" ))
    (->> (c/sysProp "java.version")
         (r/rstr rcb "usage.version.o2")
         (c/prn! "%s\n" ))
    (c/prn!! "")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- scanJars "" [out dir]

  (let [sep (c/sysProp "line.separator")]
    (reduce
      (fn [^StringBuilder b f]
         (.append b
                  (str "<classpathentry  "
                       "kind=\"lib\""
                       " path=\"" (c/fpath f) "\"/>"))
         (.append b sep))
      out
      (i/listFiles dir ".jar"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- genEclipseProj "" [pdir]

  (let [ec (io/file pdir "eclipse.projfiles")
        poddir (io/file pdir)
        pod (.getName poddir)
        sb (s/strbf<>)]
    (i/mkdirs ec)
    (FileUtils/cleanDirectory ec)
    (i/writeFile
      (io/file ec ".project")
      (-> (c/resStr (str "czlab/wabbit/eclipse/"
                         "java"
                         "/project.txt"))
          (cs/replace "${APP.NAME}" pod)
          (cs/replace "${JAVA.TEST}"
                      (c/fpath (io/file poddir
                                        "src/test/java")))
          (cs/replace "${JAVA.SRC}"
                      (c/fpath (io/file poddir
                                        "src/main/java")))
          (cs/replace "${CLJ.TEST}"
                      (c/fpath (io/file poddir
                                        "src/test/clojure")))
          (cs/replace "${CLJ.SRC}"
                      (c/fpath (io/file poddir
                                        "src/main/clojure")))))
    (i/mkdirs (io/file poddir b/dn-build "classes"))
    (doall
      (map (partial scanJars sb)
           [(io/file (getHomeDir) b/dn-dist)
            (io/file (getHomeDir) b/dn-lib)
            (io/file poddir b/dn-target)]))
    (i/writeFile
      (io/file ec ".classpath")
      (-> (c/resStr (str "czlab/wabbit/eclipse/"
                         "java"
                         "/classpath.txt"))
          (cs/replace "${CLASS.PATH.ENTRIES}" (str sb))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-IDE "" [] (onHelpXXX "usage.ide.d" 4))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onIDE
  "" {:no-doc true} [args]

  (if (and (not-empty args)
           (c/in? #{"-e" "--eclipse"} (args 0)))
    (genEclipseProj (b/getProcDir))
    (c/throwBadData "CmdError")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-ServiceSpecs "" [] (onHelpXXX "usage.svc.d" 8))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onServiceSpecs "" [args]

  (let
    [clj (Cljrt/newrt (m/getCldr) "clj")
     pfx (s/strKW :czlab.wabbit.plugs)
     specs
     {:RepeatingTimer :loops/RepeatingTimerSpec
      :OnceTimer :loops/OnceTimerSpec
      :FilePicker :files/FilePickerSpec
      :SocketIO :socket/SocketIOSpec
      :JMS :jms/JMSSpec
      :POP3 :mails/POP3Spec
      :IMAP :mails/IMAPSpec
      :HTTP :http/HTTPSpec}
     rc
     (c/preduce<map>
       #(let
          [[k s] %2
           spec (.call clj
                       (str pfx "." (s/strKW s)))]
          (assoc! %1 k spec))
       specs)]
    (c/prn!! (f/writeEdnStr rc))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onHelp-Help "" [] (c/throwBadData "CmdError"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(declare getTasks)
(defn onHelp
  "" {:no-doc true} [args]

  (let [c (keyword (first args))
        [_ h] ((getTasks) c)]
    (if (fn? h) (h) (c/throwBadData "CmdError"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(def
  ^:dynamic
  *wabbit-tasks*
  {:service [onServiceSpecs onHelp-ServiceSpecs]
   :new [onCreate onHelp-Create]
   :ide [onIDE onHelp-IDE]
   :podify [onPodify onHelp-Podify]
   :debug [onDebug onHelp-Debug]
   :help [onHelp onHelp-Help]
   :run [onStart onHelp-Start]
   :demos [onDemos onHelp-Demos]
   :generate [prnGenerate onHelp-Generate]
   :testjce [onTestJCE onHelp-TestJCE]
   :version [onVersion onHelp-Version]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- getTasks "" [] *wabbit-tasks*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


