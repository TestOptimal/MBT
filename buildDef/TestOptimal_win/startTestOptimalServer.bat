REM Set JAVA_HOME= add JDK
REM set CLASSPATH=%CLASSPATH%; add JDK
REM set PATH=%PATH%; add JDK/bin
cd %~dp0
SET PATH=%PATH%;%~dp0lib\sikulix\libs\;%~dp0lib\uia\

java -version
if %ERRORLEVEL% neq 0 goto ProcessError

REM add any additional jar files you want to use in your application that are not in lib/ folder to the cp (classpath) parameter
java -Djava.awt.headless=false -Dloader.path=lib/ -jar TestOptimal.jar  -classpath lib/ --GRAPHVIZ_DOT=lib/Graphviz/bin/dot.exe

exit /b 0

:ProcessError

SET msgboxTitle=TestOptimal Startup
SET msgboxBody=Failed to start TestOptimal Server.  Java JDK Missing!!!  
SET tmpmsgbox=%temp%\~tmpmsgbox.vbs
IF EXIST "%tmpmsgbox%" DEL /F /Q "%tmpmsgbox%"
ECHO msgbox "%msgboxBody%",0,"%msgboxTitle%">"%tmpmsgbox%"
WSCRIPT "%tmpmsgbox%"

exit /b 1
