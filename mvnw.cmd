@echo off
setlocal EnableDelayedExpansion

set DIR=%~dp0
set PROPS=%DIR%.mvn\wrapper\maven-wrapper.properties

if not exist "%PROPS%" (
    echo Missing %PROPS%
    exit /b 1
)

for /f "tokens=2 delims==" %%a in ('findstr /b "distributionUrl=" "%PROPS%"') do set DIST_URL=%%a
for %%i in ("%DIST_URL%") do set DIST_FILE=%%~ni
set DIST_FILE=%DIST_FILE%.zip
set DIST_NAME=%DIST_FILE:-bin.zip=%

set CACHE=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%
set MVN_HOME=%CACHE%\%DIST_NAME%

if not exist "%MVN_HOME%\bin\mvn.cmd" (
    if not exist "%CACHE%" mkdir "%CACHE%"
    echo Downloading Maven from %DIST_URL% ...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%CACHE%\%DIST_FILE%'"
    if errorlevel 1 (
        echo Maven download failed.
        exit /b 1
    )
    powershell -NoProfile -Command "Expand-Archive -Path '%CACHE%\%DIST_FILE%' -DestinationPath '%CACHE%' -Force"
    del "%CACHE%\%DIST_FILE%"
)

call "%MVN_HOME%\bin\mvn.cmd" %*
