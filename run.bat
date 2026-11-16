@echo off
setlocal
cd /d "%~dp0"
call compile.bat
if %ERRORLEVEL% EQU 0 (
    set JAVA_CMD=java
    if exist "%JAVA_HOME%\bin\java.exe" set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\java.exe" set JAVA_CMD="C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\java.exe"

    echo Starting Dynamis Gym Portal...
    %JAVA_CMD% -cp "bin;lib/mysql-connector-j.jar" com.gym.Main
) else (
    pause
)
