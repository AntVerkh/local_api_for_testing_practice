@echo off
setlocal EnableDelayedExpansion

title Users API Installer
color 0A

echo ===============================================
echo      Users API Auto Installer
echo ===============================================
echo.

:: Определение текущей директории (где находится install.bat)
set "INSTALL_DIR=%~dp0"
set "INSTALL_DIR=%INSTALL_DIR:~0,-1%"

echo [INFO] Installation directory: %INSTALL_DIR%
echo.

:: Проверка прав администратора
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERROR] Please run as Administrator!
    echo Right-click -> Run as administrator
    pause
    exit /b 1
)

:: Переход в директорию установки
cd /d "%INSTALL_DIR%"

:: Проверка наличия необходимых файлов
if not exist "pom.xml" (
    echo [ERROR] pom.xml not found!
    echo Please make sure you are running installer from project root directory.
    echo Current directory: %INSTALL_DIR%
    dir /b
    pause
    exit /b 1
)

echo [INFO] Project files found successfully!
echo.

:: Проверка Java
echo [INFO] Checking Java installation...
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH!
    echo Please install Java 17 or higher and try again.
    pause
    exit /b 1
)

echo [OK] Java is installed
echo.

:: Проверка Maven Wrapper
if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd not found!
    echo Please make sure Maven Wrapper files are in the project.
    pause
    exit /b 1
)

echo [OK] Maven Wrapper found
echo.

:: Скачивание зависимостей
echo [INFO] Downloading dependencies...
call mvnw dependency:resolve -q
if %errorLevel% neq 0 (
    echo [ERROR] Failed to download dependencies
    echo Please check your internet connection
    pause
    exit /b 1
)

echo [OK] Dependencies downloaded successfully
echo.

:: Сборка приложения
echo [INFO] Building application...
call mvnw clean package -DskipTests
if %errorLevel% neq 0 (
    echo [ERROR] Build failed
    echo Please check the project configuration
    pause
    exit /b 1
)

echo [OK] Application built successfully!
echo.

:: Создание необходимых скриптов
echo [INFO] Creating startup scripts...

:: Создание stop-server.bat
(
echo @echo off
echo setlocal EnableDelayedExpansion
echo title Stop Users API Server
echo echo Stopping Users API Server...
echo.
echo :: Поиск процесса Java на порту 8080
echo echo Checking for processes on port 8080...
echo set FOUND=0
echo for /f "tokens=5" %%a in ('netstat -ano ^^| findstr ":8080"') do (
echo     set /A FOUND=1
echo     set PID=%%a
echo     echo Found process with PID: !PID!
echo     echo Killing process...
echo     taskkill /PID !PID! /F
echo     echo Process killed successfully.
echo )
echo.
echo if "!FOUND!"=="0" (
echo     echo No process found running on port 8080.
echo )
echo.
echo echo Port 8080 should now be available.
echo echo.
echo pause
) > "stop-server.bat"

:: Создание VBS скрипта для запуска
(
echo Set ws = CreateObject("Wscript.Shell")
echo ws.run "cmd /c cd /d ""%INSTALL_DIR%"" && mvnw spring-boot:run", 0
echo WScript.Sleep(10000)
echo ws.run "cmd /c start http://localhost:8080/", 0
) > "start-app.vbs"

:: Создание bat файла для запуска
(
echo @echo off
echo title Users API
echo echo Starting Users API...
echo cd /d "%~dp0"
echo mvnw spring-boot:run
echo pause
) > "start-api.bat"

:: Создание файла удаления
(
echo @echo off
echo echo Uninstalling Users API...
echo echo Removing desktop shortcut...
echo if exist "%USERPROFILE%\Desktop\Users API.lnk" (
echo     del "%USERPROFILE%\Desktop\Users API.lnk"
echo     echo Desktop shortcut removed.
echo ) else (
echo     echo Desktop shortcut not found.
echo )
echo echo.
echo echo Uninstallation complete!
echo echo Note: Project files in %INSTALL_DIR% were not removed.
echo echo To completely remove, delete the folder manually.
echo echo.
echo pause
) > "uninstall.bat"

:: Создание ярлыка на рабочем столе
echo [INFO] Creating desktop shortcut...
set "SHORTCUT=%USERPROFILE%\Desktop\Users API.lnk"

:: Создание ярлыка через PowerShell
powershell -Command "$s=(New-Object -COM WScript.Shell).CreateShortcut('%SHORTCUT%'); $s.TargetPath='%INSTALL_DIR%\start-app.vbs'; $s.WorkingDirectory='%INSTALL_DIR%'; $s.IconLocation='C:\Windows\System32\SHELL32.dll,21'; $s.Description='Users API Application'; $s.Save()" >nul 2>&1

if exist "%SHORTCUT%" (
    echo [OK] Desktop shortcut created successfully
) else (
    echo [WARNING] Failed to create desktop shortcut with PowerShell
    echo [INFO] Trying alternative method...
    call :CREATE_SHORTCUT_MANUAL
)

echo.
echo ===============================================
echo    Installation Completed Successfully!
echo ===============================================
echo.
echo [OK] Application installed in: %INSTALL_DIR%
echo [OK] Desktop shortcut created: Users API.lnk
echo.
echo [INFO] To start the application:
echo        - Double-click 'Users API' on desktop
echo        - OR Run 'start-api.bat' in project folder
echo.
echo [INFO] The application will:
echo        - Start automatically when you click the desktop shortcut
echo        - Open the admin panel in your browser
echo        - Show Swagger UI documentation
echo.
echo [INFO] Access points:
echo        - Admin Panel: http://localhost:8080/
echo        - Swagger UI: http://localhost:8080/swagger-ui.html
echo        - H2 Console: http://localhost:8080/h2-console
echo        - JDBC URL: jdbc:h2:mem:testdb
echo        - Username: sa ^| Password: (empty)
echo.
echo [INFO] Default credentials:
echo        - Admin: admin/admin
echo        - User:  peasant/peasant
echo.
echo [INFO] To stop the application:
echo        - Use the 'Stop Server' button in the admin panel
echo        - OR Run 'stop-server.bat' in project folder
echo        - OR Press Ctrl+C in the terminal
echo.
echo [INFO] Project location: %INSTALL_DIR%
echo.
pause
exit /b 0

:CREATE_SHORTCUT_MANUAL
(
echo Set ws = CreateObject("WScript.Shell")
echo Set shortcut = ws.CreateShortcut("%SHORTCUT%")
echo shortcut.TargetPath = "%INSTALL_DIR%\start-app.vbs"
echo shortcut.WorkingDirectory = "%INSTALL_DIR%"
echo shortcut.IconLocation = "C:\Windows\System32\SHELL32.dll,21"
echo shortcut.Description = "Users API Application"
echo shortcut.Save
) > "%TEMP%\create_shortcut.vbs"
cscript //nologo "%TEMP%\create_shortcut.vbs" >nul 2>&1
del "%TEMP%\create_shortcut.vbs" >nul 2>&1

if exist "%SHORTCUT%" (
    echo [OK] Desktop shortcut created successfully (alternative method)
) else (
    echo [ERROR] Failed to create desktop shortcut
    echo [INFO] You can manually create shortcut to start-app.vbs
)
exit /b 0