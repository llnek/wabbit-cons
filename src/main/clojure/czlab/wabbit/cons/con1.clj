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

  (:require [czlab.twisty.codec :refer [strongPwd<> passwd<>]]
            [czlab.basal.format :refer [writeEdnStr readEdn]]
            [czlab.twisty.core :refer [assertJce]]
            [czlab.basal.resources :refer [rstr]]
            [czlab.basal.logging :as log]
            [czlab.antclj.antlib :as a]
            [clojure.java.io :as io]
            [clojure.string :as cs])

  (:use [czlab.wabbit.base.core]
        [czlab.wabbit.cons.con2]
        [czlab.basal.guids]
        [czlab.basal.core]
        [czlab.basal.str]
        [czlab.basal.io]
        [czlab.basal.meta])

  (:import [org.apache.commons.io FileUtils]
           [czlab.wabbit.cons CmdError]
           [czlab.wabbit.base Cljshim]
           [czlab.twisty IPassword]
           [java.util
            ResourceBundle
            Properties
            Calendar
            Map
            Date]
           [java.io File]
           [czlab.jasal I18N]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn bannerText
  ""
  {:no-doc true
   :tag String}
  []
  (str " __    __   ____  ____   ____   ____  ______"   "\n"
       " |  |__|  | /    ||    \\ |    \\ |    ||      |" "\n"
       " |  |  |  ||  o  ||  o  )|  o  ) |  | |      |" "\n"
       " |  |  |  ||     ||     ||     | |  | |_|  |_|" "\n"
       " |  '  '  ||  _  ||  O  ||  O  | |  |   |  |"   "\n"
       "  \\      / |  |  ||     ||     | |  |   |  |"   "\n"
       "   \\_/\\_/  |__|__||_____||_____||____|  |__|"   "\n"
       "\n"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelpXXX
  ""
  [pfx end]
  (let [rcb (I18N/base)]
    (dotimes [n end]
      (printf "%s\n" (rstr rcb
                           (str pfx (+ n 1)))))
    (println)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Create
  "" [] (onHelpXXX "usage.new.d" 5))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onCreate
  "Create a new pod"
  {:no-doc true}
  [args]
  (if (> (count args) 1)
    (createPod (args 0) (args 1))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Podify
  "" [] (onHelpXXX "usage.podify.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- bundlePod
  "Bundle an app"
  [homeDir podDir outDir]
  (let [dir (mkdirs (io/file outDir))
        a (io/file podDir)]
    (->>
      (a/antZip
        {:destFile (io/file dir (str (.getName a) ".zip"))
         :basedir a
         :includes "**/*"})
      (a/runTasks* ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onPodify
  "Package the pod"
  {:no-doc true}
  [args]
  (if-not (empty? args)
    (bundlePod (getHomeDir)
               (getProcDir) (args 0))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Start
  "" [] (onHelpXXX "usage.start.d" 4))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- runPodBg
  "Run the pod in the background"
  [homeDir podDir]
  (let
    [progW (io/file homeDir "bin/wabbit.bat")
     prog (io/file homeDir "bin/wabbit")
     tk (if (isWindows?)
          (a/antExec
            {:executable "cmd.exe"
             :dir podDir}
            [[:argvalues ["/C" "start" "/B"
                          "/MIN"
                          (fpath progW) "run"]]]))
     _ (if false
          (a/antExec
            {:executable (fpath prog)
             :dir podDir}
            [[:argvalues ["run" "bg"]]]))]
    (if (some? tk)
      (a/runTasks* tk)
      (trap! CmdError))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- startViaCons "" [a b])
(defn onStart
  "Start and run the pod"
  {:no-doc true}
  [args]
  (let [home (getHomeDir)
        cwd (getProcDir)
        s2 (first args)]
    ;; background job is handled differently on windows
    (if (and (in? #{"-bg" "--background"} s2)
             (isWindows?))
      (runPodBg home cwd)
      (do
        (println (bannerText))
        (startViaCons home cwd)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Debug
  "" [] (onHelpXXX "usage.debug.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onDebug
  "Debug the pod" {:no-doc true} [args] (onStart args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Demos
  "" [] (onHelpXXX "usage.demo.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onDemos
  "Generate demo apps"
  {:no-doc true}
  [args]
  (if-not (empty? args)
    (publishSamples (args 0))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- genPwd
  ""
  [args]
  (let [c (first args)
        n (convLong (str c) 16)]
    (if (and (>= n 8)
             (<= n 32))
      (println (.text (strongPwd<> n)))
      (trap! CmdError))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- genWwid "" [] (println (wwid<>)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- genGuid "" [] (println (uuid<>)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHash
  "Generate a hash"
  [args]
  (if-not (empty? args)
    (->> (passwd<> (first args))
         (.hashed)
         (:hash )
         (println))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onEncrypt
  "Encrypt the data"
  [args]
  (if (> (count args) 1)
    (->> (passwd<> (args 1) (args 0))
         (.encoded)
         (println))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onDecrypt
  "Decrypt the cypher"
  [args]
  (if (> (count args) 1)
    (->> (passwd<> (args 1) (args 0))
         (.text )
         (println ))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Generate
  "" [] (onHelpXXX "usage.gen.d" 8))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onGenerate
  "Generate a bunch of crypto stuff"
  {:no-doc true}
  [args]
  (let [c (first args)
        args (vec (drop 1 args))]
    (cond
      (contains? #{"-p" "--password"} c)
      (genPwd args)
      (contains? #{"-h" "--hash"} c)
      (onHash args)
      (contains? #{"-u" "--uuid"} c)
      (genGuid)
      (contains? #{"-w" "--wwid"} c)
      (genWwid)
      (contains? #{"-e" "--encrypt"} c)
      (onEncrypt args)
      (contains? #{"-d" "--decrypt"} c)
      (onDecrypt args)
      :else (trap! CmdError))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-TestJCE
  "" [] (onHelpXXX "usage.testjce.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onTestJCE
  "Test if JCE (crypto) is ok"
  {:no-doc true}
  [args]
  (let [rcb (I18N/base)]
    (assertJce)
    (println (rstr rcb "usage.testjce.ok"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-Version
  "" [] (onHelpXXX "usage.version.d" 2))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onVersion
  "Show the version of system"
  {:no-doc true}
  [args]
  (let [rcb (I18N/base)]
    (->> (sysProp "wabbit.version")
         (rstr rcb "usage.version.o1")
         (printf "%s\n" ))
    (->> (sysProp "java.version")
         (rstr rcb "usage.version.o2")
         (printf "%s\n" ))
    (println)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- scanJars
  ""
  [^StringBuilder out ^File dir]
  (let [sep (sysProp "line.separator")]
    (reduce
      (fn [^StringBuilder b f]
         (.append b
                  (str "<classpathentry  "
                       "kind=\"lib\""
                       " path=\"" (fpath f) "\"/>"))
         (.append b sep))
      out
      (listFiles dir "jar"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- genEclipseProj
  ""
  [pdir]
  (let [ec (io/file pdir "eclipse.projfiles")
        poddir (io/file pdir)
        pod (.getName poddir)
        sb (strbf<>)]
    (mkdirs ec)
    (FileUtils/cleanDirectory ec)
    (writeFile
      (io/file ec ".project")
      (-> (resStr (str "czlab/wabbit/eclipse/"
                       "java"
                       "/project.txt"))
          (cs/replace "${APP.NAME}" pod)
          (cs/replace "${JAVA.TEST}"
                      (fpath (io/file poddir
                                      "src/test/java")))
          (cs/replace "${JAVA.SRC}"
                      (fpath (io/file poddir
                                      "src/main/java")))
          (cs/replace "${CLJ.TEST}"
                      (fpath (io/file poddir
                                      "src/test/clojure")))
          (cs/replace "${CLJ.SRC}"
                      (fpath (io/file poddir
                                      "src/main/clojure")))))
    (mkdirs (io/file poddir dn-build "classes"))
    (doall
      (map (partial scanJars sb)
           [(io/file (getHomeDir) dn-dist)
            (io/file (getHomeDir) dn-lib)
            (io/file poddir dn-target)]))
    (writeFile
      (io/file ec ".classpath")
      (-> (resStr (str "czlab/wabbit/eclipse/"
                       "java"
                       "/classpath.txt"))
          (cs/replace "${CLASS.PATH.ENTRIES}" (str sb))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-IDE
  "" [] (onHelpXXX "usage.ide.d" 4))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onIDE
  "Generate IDE project files"
  {:no-doc true}
  [args]
  (if (and (not-empty args)
           (in? #{"-e" "--eclipse"} (args 0)))
    (genEclipseProj (getProcDir))
    (trap! CmdError)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- onHelp-ServiceSpecs
  "" [] (onHelpXXX "usage.svc.d" 8))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onServiceSpecs
  ""
  [args]
  (let
    [clj (Cljshim/newrt (getCldr) "clj")
     pfx (strKW :czlab.wabbit.plugs.io)
     specs
     {:loops/RepeatingTimer :loops/RepeatingTimerSpec
      :loops/OnceTimer :loops/OnceTimerSpec
      :files/FilePicker :files/FilePickerSpec
      :socket/SocketIO :socket/SocketIOSpec
      :jms/JMS :jms/JMSSpec
      :mails/POP3 :mails/POP3Spec
      :mails/IMAP :mails/IMAPSpec
      :http/WebMVC :http/WebMVCSpec
      :http/HTTP :http/HTTPSpec}
     rc
     (preduce<map>
       #(let
          [[k s] %2
           kee (keyword (str pfx "." (strKW k)))
           spec (.call clj
                       (str pfx "." (strKW s)))
           spec (update-in spec
                           [:conf]
                           assoc
                           :pluggable kee)]
          (assoc! %1
                  (keyword (name kee)) spec))
       specs)]
    (println (writeEdnStr rc))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn onHelp-Help "" [] (trap! CmdError))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(declare getTasks)
(defn onHelp
  "Show help"
  {:no-doc true}
  [args]
  (let
    [c (keyword (first args))
     [_ h] ((getTasks) c)]
    (if (fn? h)
      (h)
      (trap! CmdError))))

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
   :generate [onGenerate onHelp-Generate]
   :testjce [onTestJCE onHelp-TestJCE]
   :version [onVersion onHelp-Version]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- getTasks "" [] *wabbit-tasks*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


