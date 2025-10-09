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
set "INSTALL_DIR=%INSTALL_DIR:~0,-1%"  :: Убираем последний обратный слеш

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

:: Переменные
set "JAVA_URL=https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_windows-x64_bin.zip"
set "JAVA_ZIP=%INSTALL_DIR%\openjdk-17.zip"

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
if %errorLevel% == 0 (
    echo [OK] Java is already installed
    goto :SETUP_MAVEN_WRAPPER
)

:: Скачивание Java
echo [INFO] Downloading Java 17...
powershell -Command "Invoke-WebRequest -Uri '%JAVA_URL%' -OutFile '%JAVA_ZIP%'" >nul 2>&1

if not exist "%JAVA_ZIP%" (
    echo [ERROR] Failed to download Java
    echo Please check your internet connection and try again.
    pause
    exit /b 1
)

:: Установка Java
echo [INFO] Installing Java...
powershell -Command "Expand-Archive -Path '%JAVA_ZIP%' -DestinationPath 'java' -Force" >nul 2>&1

:: Настройка переменной среды JAVA_HOME
for /d %%i in (java\jdk-*) do (
    set "JAVA_HOME=%INSTALL_DIR%\%%i"
)

echo [INFO] Setting environment variables...
setx JAVA_HOME "%JAVA_HOME%" /m >nul
setx PATH "%JAVA_HOME%\bin;%PATH%" /m >nul

:: Очистка
del "%JAVA_ZIP%" >nul 2>&1

echo [OK] Java installed successfully

:SETUP_MAVEN_WRAPPER
echo.
echo [INFO] Setting up Maven Wrapper...

:: Создание структуры папок Maven Wrapper
if not exist ".mvn\wrapper" (
    mkdir ".mvn\wrapper" >nul 2>&1
)

:: Создание maven-wrapper.properties если его нет
if not exist ".mvn\wrapper\maven-wrapper.properties" (
    echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip > .mvn\wrapper\maven-wrapper.properties
    echo wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar >> .mvn\wrapper\maven-wrapper.properties
)

:: Создание maven-wrapper.jar если его нет
if not exist ".mvn\wrapper\maven-wrapper.jar" (
    echo [INFO] Downloading Maven Wrapper...
    powershell -Command "& { [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://wrapper-maven.appspot.com/maven-wrapper/maven-wrapper.jar')) }" >nul 2>&1
)

:: Создание mvnw.cmd если его нет
if not exist "mvnw.cmd" (
    echo [INFO] Creating Maven Wrapper script...
    call :CREATE_MVNW_CMD
)

:: Скачивание зависимостей
echo [INFO] Downloading dependencies...
call mvnw dependency:resolve -q >nul 2>&1
if %errorLevel% neq 0 (
    echo [WARNING] Failed to download dependencies with Maven Wrapper
    echo [INFO] Trying to use system Maven...
    mvn dependency:resolve -q >nul 2>&1
    if %errorLevel% neq 0 (
        echo [ERROR] Failed to download dependencies
        echo Please check your internet connection
        pause
        exit /b 1
    )
)

:: Сборка приложения
echo [INFO] Building application...
call mvnw clean package -DskipTests -q >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERROR] Build failed
    echo Please check the project configuration
    pause
    exit /b 1
)

:: Создание ярлыка
echo [INFO] Creating desktop shortcut...

set "SHORTCUT=%USERPROFILE%\Desktop\Users API.lnk"
set "TARGET=%~dp0start-app.vbs"
set "WORKDIR=%INSTALL_DIR%"

:: Создание VBS скрипта для запуска
echo Set ws = CreateObject("Wscript.Shell") > "%INSTALL_DIR%\start-app.vbs"
echo ws.run "cmd /c cd /d ""%WORKDIR%"" && mvnw spring-boot:run", 0 >> "%INSTALL_DIR%\start-app.vbs"
echo WScript.Sleep(8000) >> "%INSTALL_DIR%\start-app.vbs"
echo ws.run "cmd /c start http://localhost:8080/swagger-ui.html", 0 >> "%INSTALL_DIR%\start-app.vbs"

:: Создание ярлыка через PowerShell
powershell -Command "$s=(New-Object -COM WScript.Shell).CreateShortcut('%SHORTCUT%'); $s.TargetPath='%TARGET%'; $s.WorkingDirectory='%WORKDIR%'; $s.IconLocation='%JAVA_HOME%\bin\javaw.exe'; $s.Description='Users API Application'; $s.Save()" >nul 2>&1

:: Создание bat файла для запуска
echo @echo off > "%INSTALL_DIR%\start-api.bat"
echo title Users API >> "%INSTALL_DIR%\start-api.bat"
echo echo Starting Users API from: %INSTALL_DIR% >> "%INSTALL_DIR%\start-api.bat"
echo cd /d "%INSTALL_DIR%" >> "%INSTALL_DIR%\start-api.bat"
echo mvnw spring-boot:run >> "%INSTALL_DIR%\start-api.bat"
echo pause >> "%INSTALL_DIR%\start-api.bat"

:: Создание файла удаления
echo @echo off > "%INSTALL_DIR%\uninstall.bat"
echo echo Uninstalling Users API... >> "%INSTALL_DIR%\uninstall.bat"
echo echo Removing desktop shortcut... >> "%INSTALL_DIR%\uninstall.bat"
echo del "%USERPROFILE%\Desktop\Users API.lnk" >> "%INSTALL_DIR%\uninstall.bat"
echo echo Uninstallation complete! >> "%INSTALL_DIR%\uninstall.bat"
echo echo Note: Project files in %INSTALL_DIR% were not removed. >> "%INSTALL_DIR%\uninstall.bat"
echo echo To completely remove, delete the folder manually. >> "%INSTALL_DIR%\uninstall.bat"
echo pause >> "%INSTALL_DIR%\uninstall.bat"

:: Создание .gitignore если его нет
if not exist "%INSTALL_DIR%\.gitignore" (
    echo [INFO] Creating .gitignore file...
    call :CREATE_GITIGNORE
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
echo [INFO] The application will start automatically
echo        and open Swagger UI in your browser
echo.
echo [INFO] Access points:
echo        - Swagger UI: http://localhost:8080/swagger-ui.html
echo        - H2 Console: http://localhost:8080/h2-console
echo        - JDBC URL: jdbc:h2:mem:testdb
echo        - Username: sa | Password: (empty)
echo.
echo [INFO] Default credentials:
echo        - Admin: admin/admin
echo        - User:  peasant/peasant
echo.
echo [INFO] Project location: %INSTALL_DIR%
echo.
pause
goto :EOF

:CREATE_MVNW_CMD
(
echo @REM ----------------------------------------------------------------------------
echo @REM Maven Start Up Batch script
echo @REM
echo @REM Required ENV vars:
echo @REM JAVA_HOME - location of a JDK home dir
echo @REM
echo @REM Optional ENV vars
echo @REM M2_HOME - location of maven2's installed home dir
echo @REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
echo @REM     e.g. to debug Maven itself, use
echo @REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
echo @REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
echo @REM ----------------------------------------------------------------------------
echo.
echo @REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
echo @echo off
echo @REM set title of command window
echo title %0
echo @REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
echo @if "%%MAVEN_BATCH_ECHO%%" == "on"  echo %%MAVEN_BATCH_ECHO%%
echo.
echo @REM set %%HOME%% to equivalent of %%HOMEDRIVE%%%%HOMEPATH%%
echo if "%%HOME%%" == "" (set "HOME=%%HOMEDRIVE%%%%HOMEPATH%%")
echo.
echo set ERROR_CODE=0
echo.
echo @REM To isolate internal variables from possible post scripts, we use another setlocal
echo @setlocal
echo.
echo @REM ==== START VALIDATION ====
echo if not "%%JAVA_HOME%%" == "" goto OkJHome
echo.
echo echo.
echo echo Error: JAVA_HOME not found in your environment. >^&2
echo echo Please set the JAVA_HOME variable in your environment to match the >^&2
echo echo location of your Java installation. >^&2
echo echo.
echo goto error
echo.
echo :OkJHome
echo if exist "%%JAVA_HOME%%\bin\java.exe" goto init
echo.
echo echo.
echo echo Error: JAVA_HOME is set to an invalid directory. >^&2
echo echo JAVA_HOME = "%%JAVA_HOME%%" >^&2
echo echo Please set the JAVA_HOME variable in your environment to match the >^&2
echo echo location of your Java installation. >^&2
echo echo.
echo goto error
echo.
echo @REM ==== END VALIDATION ====
echo.
echo :init
echo @REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
echo @REM Fallback to current working directory if not found.
echo.
echo set MAVEN_PROJECTBASEDIR=%%MAVEN_BASEDIR%%
echo IF NOT "%%MAVEN_PROJECTBASEDIR%%"=="" goto endDetectBaseDir
echo.
echo set EXEC_DIR=%%CD%%
echo set WDIR=%%EXEC_DIR%%
echo :findBaseDir
echo IF EXIST "%%WDIR%%"\.mvn goto baseDirFound
echo cd ..
echo IF "%%WDIR%%"=="%%CD%%" goto baseDirNotFound
echo set WDIR=%%CD%%
echo goto findBaseDir
echo.
echo :baseDirFound
echo set MAVEN_PROJECTBASEDIR=%%WDIR%%
echo cd "%%EXEC_DIR%%"
echo goto endDetectBaseDir
echo.
echo :baseDirNotFound
echo set MAVEN_PROJECTBASEDIR=%%EXEC_DIR%%
echo cd "%%EXEC_DIR%%"
echo.
echo :endDetectBaseDir
echo.
echo IF NOT EXIST "%%MAVEN_PROJECTBASEDIR%%\.mvn\jvm.config" goto endReadAdditionalConfig
echo.
echo @setlocal EnableExtensions EnableDelayedExpansion
echo for /F "usebackq delims=" %%a in ("%%MAVEN_PROJECTBASEDIR%%\.mvn\jvm.config") do set JVM_CONFIG=!JVM_CONFIG! %%a
echo @endlocal ^& set JVM_CONFIG=%%JVM_CONFIG%%
echo.
echo :endReadAdditionalConfig
echo.
echo SET MAVEN_JAVA_EXE="%%JAVA_HOME%%\bin\java.exe"
echo set WRAPPER_JAR="%%MAVEN_PROJECTBASEDIR%%\.mvn\wrapper\maven-wrapper.jar"
echo set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
echo.
echo set DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar"
echo.
echo FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%%MAVEN_PROJECTBASEDIR%%\.mvn\wrapper\maven-wrapper.properties") DO (
echo     IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
echo )
echo.
echo @REM Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
echo @REM This allows using the maven wrapper in projects that prohibit checking in binary data.
echo if exist %%WRAPPER_JAR%% (
echo     if "%%MVNW_VERBOSE%%" == "true" (
echo         echo Found %%WRAPPER_JAR%%
echo     )
echo ) else (
echo     if not "%%MVNW_REPOURL%%" == "" (
echo         SET DOWNLOAD_URL="%%MVNW_REPOURL%%/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar"
echo     )
echo     if "%%MVNW_VERBOSE%%" == "true" (
echo         echo Couldn't find %%WRAPPER_JAR%%, downloading it ...
echo         echo Downloading from: %%DOWNLOAD_URL%%
echo     )
echo     powershell -Command "^^^(^^^"^^^&^^^{^^^"^^^$webclient = new-object System.Net.WebClient^^^;^^^"^^^"^^^$webclient.DownloadFile^^^^(^^^'%%DOWNLOAD_URL%%^^^'^^^, ^^^'%%WRAPPER_JAR%%^^^'^^^)^^^;^^^"^^^&^^^}^^^"^^^""
echo     if "%%MVNW_VERBOSE%%" == "true" (
echo         echo Finished downloading %%WRAPPER_JAR%%
echo     )
echo )
echo @REM End of extension
echo.
echo @REM Provide a "standardized" way to retrieve the CLI args that will
echo @REM work in both .bat files and in the *nix environment.
echo.
echo set MAVEN_CMD_LINE_ARGS=%%*
echo.
echo %%MAVEN_JAVA_EXE%% ^^^
echo   %%JVM_CONFIG%% ^^^
echo   %%MAVEN_OPTS%% ^^^
echo   %%MAVEN_DEBUG_OPTS%% ^^^
echo   -classpath %%WRAPPER_JAR%% ^^^
echo   "-Dmaven.multiModuleProjectDirectory=%%MAVEN_PROJECTBASEDIR%%" ^^^
echo   %%WRAPPER_LAUNCHER%% %%MAVEN_CONFIG%% %%*
echo if ERRORLEVEL 1 goto error
echo goto end
echo.
echo :error
echo set ERROR_CODE=1
echo.
echo :end
echo @endlocal ^& set ERROR_CODE=%%ERROR_CODE%%
echo.
echo if not "%%MAVEN_SKIP_RC%%" == "" goto skipRcPost
echo @REM check for post script, once with legacy .bat ending and once with .cmd ending
echo if exist "%%USERPROFILE%%\mavenrc_post.bat" call "%%USERPROFILE%%\mavenrc_post.bat"
echo if exist "%%USERPROFILE%%\mavenrc_post.cmd" call "%%USERPROFILE%%\mavenrc_post.cmd"
echo :skipRcPost
echo.
echo @REM pause the script if MAVEN_BATCH_PAUSE is set to 'on'
echo if "%%MAVEN_BATCH_PAUSE%%" == "on" pause
echo.
echo if "%%MAVEN_TERMINATE_CMD%%" == "on" exit %%ERROR_CODE%%
echo.
echo cmd /C exit /B %%ERROR_CODE%%
) > mvnw.cmd
goto :EOF

:CREATE_GITIGNORE
(
echo # Build outputs
echo target/
echo build/
echo *.jar
echo.
echo # IDE
echo .idea/
echo *.iml
echo .vscode/
echo.
echo # Logs
echo *.log
echo logs/
echo.
echo # Database
echo *.db
echo.
echo # Uploads
echo /uploads/
echo /avatars/
echo.
echo # OS files
echo .DS_Store
echo Thumbs.db
echo.
echo # Generated scripts ^(keep install.bat^)
echo start-app.vbs
echo start-api.bat
echo uninstall.bat
) > .gitignore
goto :EOF