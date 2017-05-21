;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns czlab.test.wabbit.cons.test

  (:require [czlab.basal.log :as log]
            [clojure.string :as cs]
            [clojure.java.io :as io]
            [czlab.wabbit.cons.con1 :as c1]
            [czlab.wabbit.cons.con2 :as c2]
            [czlab.wabbit.cons.con7 :as c7]
            [czlab.wabbit.base :as b]
            [czlab.basal.core :as c]
            [czlab.basal.io :as i]
            [czlab.basal.str :as s])

  (:use [clojure.test])

  (:import [java.io File]
           [czlab.jasal DataError]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(deftest czlabtestwabbitcons-test

  (is (= "hello" (b/gtid "hello")))

  (is (> (count (b/expandSysProps "${user.home}")) 0))
  (is (> (count (b/expandEnvVars "${HOME}")) 0))
  (is (= (str (b/expandSysProps "${user.home}")
              (b/expandEnvVars "${HOME}"))
         (b/expandVars "${user.home}${HOME}")))

  (is (b/precondDir i/*tempfile-repo*))
  (is (let [t (i/tempFile)
            _ (i/spitUtf8 t "hello")
            ok (b/precondFile t)]
        (i/deleteQ t)
        ok))

  (is (let [m {:s "hello.txt"
               :f (io/file "hello.txt")}]
        (and (c/ist? File (b/maybeDir m :s))
             (c/ist? File (b/maybeDir m :f)))))

  (is (let [fp (c/fpath i/*tempfile-repo*)
            _ (c/sysProp! "wabbit.user.dir" fp)
            t (i/tempFile)
            _ (i/spitUtf8 t "${pod.dir}")
            s (b/readConf t)]
        (i/deleteQ t)
        (= s fp)))

  (is (let [fp (c/fpath i/*tempfile-repo*)
            tn (c/jid<>)
            _ (b/spitXXXConf fp tn {:a 1})
            m (b/slurpXXXConf fp tn)]
        (i/deleteQ (io/file fp tn))
        (and (== 1 (count m))
             (== 1 (:a m)))))

  (is (let [fp (c/fpath i/*tempfile-repo*)
            _ (c/sysProp! "wabbit.user.dir" fp)
            tn (c/jid<>)
            _ (b/spitXXXConf fp tn {:a "${pod.dir}"})
            m (b/slurpXXXConf fp tn true)]
        (i/deleteQ (io/file fp tn))
        (and (== 1 (count m))
             (string? (:a m))
             (> (count (:a m)) 0))))

  (is (== 17 (-> (c1/onGenerate ["--password" "17"] )
                 (s/trimr "\n")
                 count)))
  (is (== 13 (-> (c1/onGenerate ["-p" "13"] )
                 (s/trimr "\n")
                 count)))

  (is (> (-> (c1/onGenerate ["--hash" "hello"])
             (s/trimr "\n")
             count) 0))
  (is (> (-> (c1/onGenerate ["-h" "hello"])
             (s/trimr "\n")
             count) 0))

  (is (> (-> (c1/onGenerate ["--uuid"])
             (s/trimr "\n")
             count) 0))
  (is (> (-> (c1/onGenerate ["-u"])
             (s/trimr "\n")
             count) 0))

  (is (> (-> (c1/onGenerate ["--wwid"])
             (s/trimr "\n")
             count) 0))
  (is (> (-> (c1/onGenerate ["-w"])
             (s/trimr "\n")
             count) 0))

  (is (let [e (-> (c1/onGenerate ["--encrypt" "secret" "hello"])
                  (s/trimr "\n"))
            d (-> (c1/onGenerate ["--decrypt" "secret" e])
                  (s/trimr "\n"))]
        (= d "hello")))

  (is (let [e (-> (c1/onGenerate ["-e" "secret" "hello"])
                  (s/trimr "\n"))
            d (-> (c1/onGenerate ["-d" "secret" e])
                  (s/trimr "\n"))]
        (= d "hello")))

  (is (thrown? DataError (c1/onGenerate ["-bbbbb"])))

  (is (string? "That's all folks!")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF

