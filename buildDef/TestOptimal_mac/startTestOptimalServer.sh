#!/bin/sh

java -Dloader.path=lib/ -Djava.awt.headless=false -jar TestOptimal_MBT.jar -classpath lib/

status=$?
if [ $status -eq 1 ]; then
    echo "TestOptimal MBT server failed to start, check log/tosvr.log for details of the error"
elif [ $status -eq 127 ]; then
    echo "JDK17 or later is required!"
fi