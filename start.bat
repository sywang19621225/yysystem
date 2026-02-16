@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%yysystem-backend"
set "WEB_DIR=%ROOT%yysystem-web"
set "MODE=%~1"

echo ===========================================================
echo YYSystem Startup
echo ===========================================================
echo.

call :select_java
if errorlevel 1 goto end_fail
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA_HOME=%JAVA_HOME%
echo.

call :kill_port 8080
call :kill_port 3000
timeout /t 2 /nobreak >nul

if not exist "%BACKEND_DIR%\pom.xml" (
  echo [backend] pom.xml not found: %BACKEND_DIR%
  goto end_fail
)
if not exist "%WEB_DIR%\package.json" (
  echo [frontend] package.json not found: %WEB_DIR%
  goto end_fail
)

echo [backend] starting...
start "Backend-8080" cmd /k "cd /d ""%BACKEND_DIR%"" & call start-backend.bat"
echo.

echo [check] waiting for backend (max 90s)...
set /a count=0
:wait_backend_loop
netstat -ano 2>nul | findstr ":8080" | findstr "LISTENING" >nul
if not errorlevel 1 (
  echo [backend] Ready on port 8080
  goto backend_ready
)
set /a count+=1
if %count% GEQ 90 (
  echo [backend] Timeout waiting for port 8080
  goto end_fail
)
timeout /t 1 /nobreak >nul
goto wait_backend_loop

:backend_ready
echo.
echo [frontend] starting...
start "Frontend-3000" cmd /k "cd /d ""%WEB_DIR%"" & npm run dev"
echo.

echo [check] waiting for frontend (max 60s)...
set /a count=0
:wait_frontend_loop
netstat -ano 2>nul | findstr ":3000" | findstr "LISTENING" >nul
if not errorlevel 1 (
  echo [frontend] Ready on port 3000
  goto frontend_ready
)
set /a count+=1
if %count% GEQ 60 (
  echo [frontend] Timeout waiting for port 3000
  goto end_fail
)
timeout /t 1 /nobreak >nul
goto wait_frontend_loop

:frontend_ready
echo.
echo ===========================================================
echo READY
echo backend:  http://localhost:8080
echo frontend: http://localhost:3000
echo ===========================================================
echo.
if /i "%MODE%"=="--auto" exit /b 0
pause >nul
exit /b 0

:end_fail
echo.
echo FAILED - see messages above.
echo.
if /i "%MODE%"=="--auto" exit /b 1
pause >nul
exit /b 1

:kill_port
set "PORT=%~1"
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":%~1 "') do (
  taskkill /F /PID %%P >nul 2>&1
)
exit /b 0

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
echo No JDK 17+ found. Set JAVA_HOME to a JDK 17+ directory.
exit /b 1

:java_major
set "JAVA_MAJOR=0"
for /f "tokens=2" %%V in ('"%~1" -version 2^>^&1') do (
  for /f "tokens=1 delims=." %%M in ("%%V") do set /a JAVA_MAJOR=%%M
)
exit /b 0
