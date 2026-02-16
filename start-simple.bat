@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%yysystem-backend"
set "WEB_DIR=%ROOT%yysystem-web"

echo ===========================================================
echo YYSystem Startup (Simple)
echo ===========================================================
echo.

:: Check Java
call :select_java
if errorlevel 1 (
  echo Java check failed.
  pause
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA_HOME=%JAVA_HOME%
echo.

:: Kill existing ports
echo [1] Stopping existing services...
call :kill_port 8080
call :kill_port 3000
timeout /t 2 /nobreak >nul
echo.

:: Check files exist
if not exist "%BACKEND_DIR%\pom.xml" (
  echo [ERROR] pom.xml not found: %BACKEND_DIR%
  pause
  exit /b 1
)
if not exist "%WEB_DIR%\package.json" (
  echo [ERROR] package.json not found: %WEB_DIR%
  pause
  exit /b 1
)

:: Start Backend
echo [2] Starting backend...
start "Backend-8080" cmd /k "cd /d ""%BACKEND_DIR%"" & call start-backend.bat"
echo.

:: Wait for backend to be ready (check port 8080)
echo [3] Waiting for backend (port 8080)...
set /a retry=60
:wait_backend
netstat -ano | findstr ":8080 .*LISTENING" >nul 2>&1
if not errorlevel 1 (
  echo [backend] Port 8080 is listening
goto backend_ready
)
set /a retry-=1
if %retry% LEQ 0 (
  echo [ERROR] Backend failed to start on port 8080
  pause
  exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_backend

:backend_ready
echo [backend] Backend is ready!
echo.

:: Start Frontend
echo [4] Starting frontend...
start "Frontend-3000" cmd /k "cd /d ""%WEB_DIR%"" & npm run dev"
echo.

:: Wait for frontend to be ready (check port 3000)
echo [5] Waiting for frontend (port 3000)...
set /a retry=60
:wait_frontend
netstat -ano | findstr ":3000 .*LISTENING" >nul 2>&1
if not errorlevel 1 (
  echo [frontend] Port 3000 is listening
goto frontend_ready
)
set /a retry-=1
if %retry% LEQ 0 (
  echo [ERROR] Frontend failed to start on port 3000
  pause
  exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_frontend

:frontend_ready
echo [frontend] Frontend is ready!
echo.

echo ===========================================================
echo READY
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:3000
echo ===========================================================
pause
exit /b 0

:kill_port
set "PORT=%~1"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%PORT% " 2^>nul') do taskkill /F /PID %%P >nul 2>&1
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
