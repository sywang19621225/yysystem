@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%yysystem-backend"
set "WEB_DIR=%ROOT%yysystem-web"

echo.
echo ============================================
echo YYSystem Startup (Enhanced)
echo ============================================
echo.

echo [0] Java...
call :select_java
if errorlevel 1 (
  echo Java check failed.
  pause >nul
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA_HOME=%JAVA_HOME%
echo.

echo [1] Free ports...
call :kill_port 8080
call :kill_port 3000
echo.

if not exist "%BACKEND_DIR%\pom.xml" (
  echo backend pom.xml not found: %BACKEND_DIR%
  pause >nul
  exit /b 1
)
if not exist "%WEB_DIR%\package.json" (
  echo frontend package.json not found: %WEB_DIR%
  pause >nul
  exit /b 1
)

echo [2] Backend...
start "Backend-8080" cmd /c "cd /d ""%BACKEND_DIR%"" & call start-backend.bat > backend.log 2>&1"
echo backend log: %BACKEND_DIR%\backend.log
echo.

echo [3] Waiting for backend to be ready...
call :wait_http backend 8080 /status 200 90
if errorlevel 1 exit /b 1
echo.

echo [4] Frontend (backend is ready)...
start "Frontend-3000" cmd /c "cd /d ""%WEB_DIR%"" & npm run dev > frontend.log 2>&1"
echo frontend log: %WEB_DIR%\frontend.log
echo.

echo [5] Waiting for frontend...
call :wait_http frontend 3000 / 200 60
if errorlevel 1 exit /b 1
echo.

echo ============================================
echo READY
echo frontend: http://localhost:3000
echo backend:  http://localhost:8080
echo status:   http://localhost:8080/status
echo ============================================
echo.
pause >nul
exit /b 0

:kill_port
set "PORT=%~1"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R ":%PORT% " 2^>nul') do taskkill /F /PID %%P >nul 2>&1
exit /b 0

:wait_http
set "NAME=%~1"
set "PORT=%~2"
set "PATH_PART=%~3"
set "EXPECT=%~4"
set "RETRY=%~5"
set "URL=http://localhost:%PORT%%PATH_PART%"
call :wait_port "%NAME%" "%PORT%" "%RETRY%"
if errorlevel 1 exit /b 1
where curl.exe >nul 2>&1
if not errorlevel 1 (
  curl.exe -s -o NUL -I "%URL%" 2>nul
  if not errorlevel 1 (
    echo [%NAME%] HTTP OK %URL%
  ) else (
    echo [%NAME%] LISTEN OK but HTTP failed %URL%
  )
) else (
  echo [%NAME%] LISTEN OK :%PORT%
)
exit /b 0

:wait_port
set "NAME=%~1"
set "PORT=%~2"
set "RETRY=%~3"
:wait_port_loop
netstat -ano ^| findstr /R ":%PORT% .*LISTENING" >nul 2>&1
if not errorlevel 1 (
  echo [%NAME%] LISTEN :%PORT%
  exit /b 0
)
set /a RETRY-=1
if %RETRY% LEQ 0 (
  echo [%NAME%] NOT LISTEN :%PORT%
  exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_port_loop

:select_java
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" (
    call :java_major "%JAVA_HOME%\bin\javac.exe"
    if !JAVA_MAJOR! GEQ 17 exit /b 0
  )
)
if exist "E:\Program Files\Java\jdk-21.0.8\bin\javac.exe" (
  set "JAVA_HOME=E:\Program Files\Java\jdk-21.0.8"
  exit /b 0
)
set "BEST_JAVAC="
set /a BEST_VER=0
for /f "delims=" %%J in ('where javac 2^>nul') do (
  for /f "tokens=2" %%V in ('"%%J" -version 2^>^&1') do (
    for /f "tokens=1 delims=." %%M in ("%%V") do (
      set /a CUR_VER=%%M
      if !CUR_VER! GEQ 17 (
        if !CUR_VER! GEQ !BEST_VER! (
          set /a BEST_VER=!CUR_VER!
          set "BEST_JAVAC=%%J"
        )
      )
    )
  )
)
if not defined BEST_JAVAC (
  echo No JDK 17+ found. Set JAVA_HOME to a JDK 17+ directory.
  exit /b 1
)
for %%I in ("%BEST_JAVAC%") do set "JAVA_HOME=%%~dpI.."
for %%H in ("%JAVA_HOME%") do set "JAVA_HOME=%%~fH"
exit /b 0

:java_major
set "JAVA_MAJOR=0"
for /f "tokens=2" %%V in ('"%~1" -version 2^>^&1') do (
  for /f "tokens=1 delims=." %%M in ("%%V") do set /a JAVA_MAJOR=%%M
)
exit /b 0

