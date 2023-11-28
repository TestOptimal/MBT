#!/bin/sh
if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo version "$version"
    if [[ "$version" > "1.8" ]]; then
       cd "$(dirname "$0")"
       # --GRAPHVIZ_DOT=lib/Graphviz/bin/
       pwd
       java -Dloader.path=lib/ -Djava.awt.headless=false -jar TestOptimal.jar -classpath lib/
    else
        echo Java version is less than 1.8.
    fi
else
    echo Java is not installed!!!
fi
