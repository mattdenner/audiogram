(defproject audiogram "0.1.0-SNAPSHOT"
  :description "An application to determine your audiogram"
  :url "http://github.com/mattdenner/audiogram"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories  {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [overtone "0.8.1"]
                 ]
  :main audiogram.core)
