;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defproject io.czlab/wabbit-cons "1.0.0"

  :license {:url "http://www.eclipse.org/legal/epl-v10.html"
            :name "Eclipse Public License"}

  :description ""
  :url "https://github.com/llnek/wabbit-cons"

  :dependencies [[io.czlab/wabbit-shared "1.0.0"]
                 [io.czlab/wabbit-base "1.0.0"]
                 [io.czlab/wabbit-plugs "1.0.0"]
                 [io.aviso/pretty "0.1.33"]
                 [stencil "0.5.0"]
                 [io.czlab/antclj "1.0.0"]
                 [io.czlab/twisty "1.0.0"]]

  :plugins [[lein-codox "0.10.2"]
            [lein-pprint "1.1.2"]]

  :main czlab.wabbit.cons.con7

  :profiles {:provided {:dependencies
                        [[org.clojure/clojure "1.8.0" :scope "provided"]
                         [net.mikera/cljunit "0.6.0" :scope "test"]
                         [junit/junit "4.12" :scope "test"]
                         [codox/codox "0.10.2" :scope "provided"]]}
             :run {:global-vars ^:replace {*warn-on-reflection* false}}
             :uberjar {:aot :all}}

  :global-vars {*warn-on-reflection* true}
  :target-path "out/%s"
  :aot :all
  ;;:jar-exclusions [#"(?:^|/).svn/"]
  :coordinate! "czlab/wabbit/cons"
  :omit-source true

  :java-source-paths ["src/main/java" "src/test/java"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  ;;:resource-paths ["src/main/resources"]

  :jvm-opts ["-Dlog4j.configurationFile=file:attic/log4j2.xml"]
  :javac-options ["-source" "8"
                  "-Xlint:unchecked" "-Xlint:-options" "-Xlint:deprecation"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF

