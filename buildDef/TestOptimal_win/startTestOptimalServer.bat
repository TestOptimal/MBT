
java -Djava.awt.headless=false -Dloader.path=lib/ -jar TestOptimal_MBT.jar  -classpath lib/ --GRAPHVIZ_DOT=lib/Graphviz/bin/dot.exe

exit /b 0

:ProcessError

SET msgboxTitle=TestOptimal Startup
SET msgboxBody=Failed to start TestOptimal MBT Server.  Java JDK Missing!!!  
SET tmpmsgbox=%temp%\~tmpmsgbox.vbs
IF EXIST "%tmpmsgbox%" DEL /F /Q "%tmpmsgbox%"
ECHO msgbox "%msgboxBody%",0,"%msgboxTitle%">"%tmpmsgbox%"
WSCRIPT "%tmpmsgbox%"

exit /b 1
