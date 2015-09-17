@ECHO off
@REM SCM Manager started script

if not "%SCM_MANAGER_HOME%" == "" goto JAVA_HOME
set SCM_MANAGER_HOME=%~dp0\..

:JAVA_HOME
if not "%JAVA_HOME%" == "" goto JAVA_HOME_OK
set JAVA=java
echo JAVA_HOME is not set. Unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK/JRE to avoid this message.
goto START

:JAVA_HOME_OK
set JAVA=%JAVA_HOME%\bin\java

:START
"%JAVA%" -jar "%SCM_MANAGER_HOME%\svn-manager-client-${project.version}.jar" %*
:END