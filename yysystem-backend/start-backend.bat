@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
cd /d "%ROOT%"

call :select_java
if errorlevel 1 (
  echo ERROR: No usable JDK found. Please set JAVA_HOME to a JDK 17+ directory.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA_HOME=%JAVA_HOME%
echo.

call :kill_port 8080

mvn -DskipTests spring-boot:run
exit /b %errorlevel%

:select_java
if exist "E:\Program Files\Java\jdk-21.0.8\bin\java.exe" (
  set "JAVA_HOME=E:\Program Files\Java\jdk-21.0.8"
  exit /b 0
)

if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" (
    call :java_major "%JAVA_HOME%\bin\java.exe"
    if !JAVA_MAJOR! GEQ 17 exit /b 0
  )
)

for /f "delims=" %%J in ('where java 2^>nul') do (
  for %%I in ("%%J") do set "JAVA_HOME=%%~dpI.."
  for %%H in ("!JAVA_HOME!") do set "JAVA_HOME=%%~fH"
  if exist "!JAVA_HOME!\bin\java.exe" (
    call :java_major "!JAVA_HOME!\bin\java.exe"
    if !JAVA_MAJOR! GEQ 17 exit /b 0
  )
)

exit /b 1

:java_major
set "JAVA_MAJOR=0"
for /f "tokens=3" %%V in ('"%~1" -version 2^>^&1 ^| findstr /I "version"') do (
  set "VER=%%~V"
  set "VER=!VER:\"=!"
  for /f "tokens=1 delims=." %%M in ("!VER!") do set /a JAVA_MAJOR=%%M
)
exit /b 0

:kill_port
set "PORT=%~1"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R ":%PORT% " 2^>nul') do taskkill /F /PID %%P >nul 2>&1
exit /b 0

