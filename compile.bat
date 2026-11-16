@echo off
setlocal
cd /d "%~dp0"
if not exist "bin" mkdir bin

set JAVAC_CMD=javac
if exist "%JAVA_HOME%\bin\javac.exe" set JAVAC_CMD="%JAVA_HOME%\bin\javac.exe"
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\javac.exe" set JAVAC_CMD="C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\javac.exe"

echo Compiling Gym Authentication System using %JAVAC_CMD%...
%JAVAC_CMD% -encoding UTF-8 -cp "lib/mysql-connector-j.jar" -d bin src/com/gym/model/*.java src/com/gym/util/*.java src/com/gym/config/*.java src/com/gym/dao/*.java src/com/gym/session/*.java src/com/gym/ui/*.java src/com/gym/ui/dashboard/*.java src/com/gym/*.java
if %ERRORLEVEL% EQU 0 (
    if exist db.properties copy /Y db.properties bin\db.properties >nul
    echo [SUCCESS] Compilation completed successfully into bin/
) else (
    echo [ERROR] Compilation failed.
)
