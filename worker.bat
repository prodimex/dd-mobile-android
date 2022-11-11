@echo off

setlocal
call :setESC

cls
echo %ESC%[101;93m STYLES  %ESC%[0m
echo ^<ESC^>[0m  %ESC%[0mReset %ESC%[0m
echo ^<ESC^>[1m %ESC%[1mBold %ESC%[0m
echo ^<ESC^>[4m %ESC%[4mUnderline %ESC%[0m
echo ^<ESC^>[7m %ESC%[7mInverse %ESC%[0m
echo.
echo %ESC%[101;93m NORMAL FOREGROUND COLORS  %ESC%[0m
echo ^<ESC^>[30m %ESC%[30mBlack %ESC%[0m (black)
echo ^<ESC^>[31m %ESC%[31mRed %ESC%[0m
echo ^<ESC^>[32m %ESC%[32mGreen %ESC%[0m
echo ^<ESC^>[33m %ESC%[33mYellow %ESC%[0m
echo ^<ESC^>[34m %ESC%[34mBlue %ESC%[0m
echo ^<ESC^>[35m %ESC%[35mMagenta %ESC%[0m
echo ^<ESC^>[36m %ESC%[36mCyan %ESC%[0m
echo ^<ESC^>[37m %ESC%[37mWhite %ESC%[0m
echo.
echo %ESC%[101;93m NORMAL BACKGROUND COLORS  %ESC%[0m
echo ^<ESC^>[40m %ESC%[40mBlack %ESC%[0m
echo ^<ESC^>[41m %ESC%[41mRed %ESC%[0m
echo ^<ESC^>[42m %ESC%[42mGreen %ESC%[0m
echo ^<ESC^>[43m %ESC%[43mYellow %ESC%[0m
echo ^<ESC^>[44m %ESC%[44mBlue %ESC%[0m
echo ^<ESC^>[45m %ESC%[45mMagenta %ESC%[0m
echo ^<ESC^>[46m %ESC%[46mCyan %ESC%[0m
echo ^<ESC^>[47m %ESC%[47mWhite %ESC%[0m (white)
echo.
echo %ESC%[101;93m STRONG FOREGROUND COLORS  %ESC%[0m
echo ^<ESC^>[90m %ESC%[90mWhite %ESC%[0m
echo ^<ESC^>[91m %ESC%[91mRed %ESC%[0m
echo ^<ESC^>[92m %ESC%[92mGreen %ESC%[0m
echo ^<ESC^>[93m %ESC%[93mYellow %ESC%[0m
echo ^<ESC^>[94m %ESC%[94mBlue %ESC%[0m
echo ^<ESC^>[95m %ESC%[95mMagenta %ESC%[0m
echo ^<ESC^>[96m %ESC%[96mCyan %ESC%[0m
echo ^<ESC^>[97m %ESC%[97mWhite %ESC%[0m
echo.
echo %ESC%[101;93m STRONG BACKGROUND COLORS  %ESC%[0m
echo ^<ESC^>[100m %ESC%[100mBlack %ESC%[0m
echo ^<ESC^>[101m %ESC%[101mRed %ESC%[0m
echo ^<ESC^>[102m %ESC%[102mGreen %ESC%[0m
echo ^<ESC^>[103m %ESC%[103mYellow %ESC%[0m
echo ^<ESC^>[104m %ESC%[104mBlue %ESC%[0m
echo ^<ESC^>[105m %ESC%[105mMagenta %ESC%[0m
echo ^<ESC^>[106m %ESC%[106mCyan %ESC%[0m
echo ^<ESC^>[107m %ESC%[107mWhite %ESC%[0m
echo.
echo %ESC%[101;93m COMBINATIONS  %ESC%[0m
echo ^<ESC^>[31m                     %ESC%[31mred foreground color %ESC%[0m
echo ^<ESC^>[7m                      %ESC%[7minverse foreground ^<-^> background %ESC%[0m
echo ^<ESC^>[7;31m                   %ESC%[7;31minverse red foreground color %ESC%[0m
echo ^<ESC^>[7m and nested ^<ESC^>[31m %ESC%[7mbefore %ESC%[31mnested %ESC%[0m
echo ^<ESC^>[31m and nested ^<ESC^>[7m %ESC%[31mbefore %ESC%[7mnested %ESC%[0m
::pause
cls

:COMPILATION_SELECTOR
Echo ===========================================================================
Echo    [%ESC%[36m 1 %ESC%[0m] Build loader debug app and install and run
Echo    [%ESC%[36m 11 %ESC%[0m] Prepare debug to upload loader
Echo.
Echo    [%ESC%[36m 2 %ESC%[0m] Build driver debug app and install and run
Echo    [%ESC%[36m 21 %ESC%[0m] Prepare debug to upload driver
Echo.
Echo    [%ESC%[36m 3 %ESC%[0m] Build develop debug app and install and run
Echo    [%ESC%[36m 31 %ESC%[0m] Prepare debug to upload develop
Echo.
Echo    [%ESC%[36m 41 %ESC%[0m] Prepare release to upload loader
Echo    [%ESC%[36m 42 %ESC%[0m] Prepare release to upload driver
Echo    [%ESC%[36m 43 %ESC%[0m] Prepare release to upload develop
Echo.
Echo    [%ESC%[36m 5 %ESC%[0m] Prepare all to upload on release
Echo    [%ESC%[36m 51 %ESC%[0m] Prepare all to upload on debug
Echo.
Echo    [%ESC%[36m 60 %ESC%[0m] Up version
Echo    [%ESC%[36m 61 %ESC%[0m] Down version
Echo.
Echo ===========================================================================
Echo    SELECT:
Set Input=0918239021309810982093809
Set /P Input="-->"

if %Input% == 1 (
    CALL :CHANGE_BUILD_CONFIG loader
	GOTO :DEV_BUILD_AND_INSTALL_AND_RUN
)
if %Input% == 11 (
    CALL :BUILD_APP_TO_MODE_BY_VERSION debug loader
    GOTO :COMPILATION_SELECTOR
)

if %Input% == 2 (
    CALL :CHANGE_BUILD_CONFIG driver
    GOTO :DEV_BUILD_AND_INSTALL_AND_RUN
)
if %Input% == 21 (
    CALL :BUILD_APP_TO_MODE_BY_VERSION debug driver
    GOTO :COMPILATION_SELECTOR
)

if %Input% == 3 (
    CALL :CHANGE_BUILD_CONFIG develop
    GOTO :DEV_BUILD_AND_INSTALL_AND_RUN
)

if %Input% == 31 (
    CALL :BUILD_APP_TO_MODE_BY_VERSION debug develop
    GOTO :COMPILATION_SELECTOR
)

if %Input% == 41 (
    CALL :BUILD_APP_TO_MODE_BY_VERSION release loader
    GOTO :COMPILATION_SELECTOR
)
if %Input% == 42 (
    CALL :BUILD_APP_TO_MODE_BY_VERSION release driver
    GOTO :COMPILATION_SELECTOR
)
if %Input% == 43 (
    CALL :BUILD_APP_TO_MODE_BY_VERSION release develop
    GOTO :COMPILATION_SELECTOR
)

if %Input% == 5 (
    @echo off

    CALL :BUILD_APP_TO_MODE_BY_VERSION release develop
    CALL :BUILD_APP_TO_MODE_BY_VERSION release driver
    CALL :BUILD_APP_TO_MODE_BY_VERSION release loader

    GOTO :COMPILATION_SELECTOR
)
if %Input% == 51 (
    @echo off

    CALL :BUILD_APP_TO_MODE_BY_VERSION debug develop
    CALL :BUILD_APP_TO_MODE_BY_VERSION debug driver
    CALL :BUILD_APP_TO_MODE_BY_VERSION debug loader

    GOTO :COMPILATION_SELECTOR
)


if %Input% == 60 (
    CALL :UP_VERSION
    GOTO :COMPILATION_SELECTOR
)
if %Input% == 61 (
    CALL :DOWN_VERSION
    GOTO :COMPILATION_SELECTOR
)
GOTO :COMPILATION_SELECTOR

    ::CALL :CHANGE_BUILD_CONFIG develop
    ::start /WAIT cmd /k "gradlew assembleRelease && exit"
    ::CALL :COPY_TO develop release

    ::CALL :CHANGE_BUILD_CONFIG driver
    ::start /WAIT cmd /k "gradlew assembleRelease && exit"
    ::CALL :COPY_TO driver release

    ::CALL :CHANGE_BUILD_CONFIG loader
    ::start /WAIT cmd /k "gradlew assembleRelease && exit"
    ::CALL :COPY_TO loader release

 ::copy /y "%FileURI%" "%TargetDir%\%NewFileName%"
    ::start cmd /k "gradlew assembleRelease && copy .\app\build\outputs\apk\release\app-release.apk .\upload\develop\version.apk && pause"

::setlocal EnableDelayedExpansion
      ::for /F "delims=" %%a in ('cscript readversionname.vbs') do (endlocal & set appVersionName=%%a)

::if %Input% == 33 (
   :: start cmd /k "cscript /nologo upver.vbs  > .\app\build.txt && cd app && ren build.gradle build.gradle.bkup && del build.gradle.bkup && ren build.txt build.gradle && exit"
::)


:BUILD_APP_TO_MODE_BY_VERSION
    Set Mode=%1
    Set BuildVersion=%2
    Echo %ESC%[41m Building application to %BuildVersion% in %Mode% mode started %ESC%[0m

    del .\app\src\main\java\ru\prodimex\digitaldispatcher\AppConfig.kt
    copy .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\%BuildVersion%.txt .\app\src\main\java\ru\prodimex\digitaldispatcher\AppConfig.kt

    del .\app\src\main\res\values\strings.xml
    Echo .\app\src\main\res\values\%BuildVersion%.xml
    copy .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\%BuildVersion%.xml .\app\src\main\res\values\strings.xml

    del .\app\src\main\res\drawable-v24\ic_launcher_foreground.xml
    Echo .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\icon_%BuildVersion%.xml
    copy .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\icon_%BuildVersion%.xml .\app\src\main\res\drawable-v24\ic_launcher_foreground.xml
    Echo   %ESC%[42m BUILD CONFIGURATION CHANGED TO %BuildVersion% %ESC%[0m

    if %Mode% == release (
        start /WAIT cmd /k "gradlew assembleRelease && exit"
    )
    if %Mode% == debug (
        start /WAIT cmd /k "gradlew assembleDebug && exit"
    )

    setlocal EnableDelayedExpansion
    for /F "delims=" %%a in ('cscript readversionname.vbs') do (endlocal & set appVersionName=%%a)
    move .\app\build\outputs\apk\%Mode%\app-%Mode%.apk .\upload\%appVersionName%-%BuildVersion%-%Mode%.apk
    Echo Copied %appVersionName% %BuildVersion% \upload\%appVersionName%-%BuildVersion%-%Mode%.apk

    Echo %ESC%[41m Building application to %BuildVersion% in %Mode% mode completed %ESC%[0m
    TIMEOUT 2
EXIT /b

:CHANGE_BUILD_CONFIG
    Set TargetConfig=%1
    del .\app\src\main\java\ru\prodimex\digitaldispatcher\AppConfig.kt
    copy .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\%TargetConfig%.txt .\app\src\main\java\ru\prodimex\digitaldispatcher\AppConfig.kt

    del .\app\src\main\res\values\strings.xml
    Echo .\app\src\main\res\values\%TargetConfig%.xml
    copy .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\%TargetConfig%.xml .\app\src\main\res\values\strings.xml

    del .\app\src\main\res\drawable-v24\ic_launcher_foreground.xml
    Echo .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\icon_%TargetConfig%.xml
    copy .\app\src\main\java\ru\prodimex\digitaldispatcher\configs\icon_%TargetConfig%.xml .\app\src\main\res\drawable-v24\ic_launcher_foreground.xml

    Echo   %ESC%[42m BUILD CONFIGURATION CHANGED TO %TargetConfig% %ESC%[0m
EXIT /b
rename  .\app\src\main\res\values\strings.xml strings.xml.bkup
    rename .\app\src\main\res\values\strings.txt strings.xml
del .\app\build.gradle.bkup
:UP_VERSION
    cscript /nologo upver.vbs > .\app\build.txt
    rename  .\app\build.gradle build.gradle.bkup
    rename .\app\build.txt build.gradle
    del .\app\build.gradle.bkup

    setlocal EnableDelayedExpansion
    for /F "delims=" %%a in ('cscript readversionname.vbs') do (endlocal & set appVersionName=%%a)
    Echo   %ESC%[42m VERSION UPPED TO %appVersionName% %ESC%[0m
    Echo ===========================================================================
EXIT /b
:DOWN_VERSION
    cscript /nologo downver.vbs > .\app\build.txt
    rename  .\app\build.gradle build.gradle.bkup
    rename .\app\build.txt build.gradle
    del .\app\build.gradle.bkup

    setlocal EnableDelayedExpansion
    for /F "delims=" %%a in ('cscript readversionname.vbs') do (endlocal & set appVersionName=%%a)
    Echo   %ESC%[42m VERSION DOWNED TO %appVersionName% %ESC%[0m
    Echo ===========================================================================
EXIT /b

:PREPARE_TO_UPLOAD
    Set Mode=%1
    Set AppVersion=%2
    setlocal EnableDelayedExpansion
    for /F "delims=" %%a in ('cscript readversionname.vbs') do (endlocal & set appVersionName=%%a)
    move .\app\build\outputs\apk\%Mode%\app-%Mode%.apk .\upload\%Destination%\%appVersionName%-%Mode%.apk
    Echo Copied %appVersionName% %Destination% \upload\%Destination%\%appVersionName%-%Mode%.apk
EXIT /b

:COPY_TO
    Set Destination=%1
    Set Mode=%2
    setlocal EnableDelayedExpansion
    for /F "delims=" %%a in ('cscript readversionname.vbs') do (endlocal & set appVersionName=%%a)
    move .\app\build\outputs\apk\%Mode%\app-%Mode%.apk .\upload\%Destination%\%appVersionName%-%Mode%.apk
    Echo Copied %appVersionName% %Destination% \upload\%Destination%\%appVersionName%-%Mode%.apk
EXIT /b

:DEV_BUILD_AND_INSTALL_AND_RUN
    ::CALL :UP_VERSION
    start /WAIT cmd /k "gradlew assembleDebug && gradlew installDebug && exit"
    Echo   %ESC%[42m APPLICATION COMPILED AND INSTALLED %ESC%[0m
    %ANDROID_HOME%\platform-tools\adb.exe shell am start -n ru.prodimex.digitaldispatcher/ru.prodimex.digitaldispatcher.Main
    Echo   %ESC%[42m APPLICATION STARTED %ESC%[0m
    ::gradlew assembleDebug && gradlew installDebug && %ANDROID_HOME%\platform-tools\adb.exe shell am start -n ru.prodimex.digitaldispatcher/ru.prodimex.digitaldispatcher.Main && exit
::start cmd /k "cscript /nologo upver.vbs  > .\app\build.txt && cd app && ren build.gradle build.gradle.bkup && del build.gradle.bkup && ren build.txt build.gradle && cd ../ && gradlew assembleDebug && gradlew installDebug && %ANDROID_HOME%\platform-tools\adb.exe shell am start -n ru.prodimex.digitaldispatcher/ru.prodimex.digitaldispatcher.Main && exit"
GOTO :COMPILATION_SELECTOR

pause

Set WorkRoot=C:\Work
Set ConEmuBaseDir=C:\ConEmu\ConEmu
:PROJECT_SELECTION
cls
Echo ===========================================================================
Echo    SELECT PROJECT:

Echo    [%ESC%[36m 1 %ESC%[0m] dd-mobile
Echo    [%ESC%[36m 7 %ESC%[0m] dd-loader
::Echo    [%ESC%[36m 2 %ESC%[0m] digital-dispatcher-tms
Echo.
Echo    [%ESC%[36m 3 %ESC%[0m] dd-factory-backend
Echo    [%ESC%[36m 4 %ESC%[0m] dd-factory-frontend
Echo.
::Echo    [%ESC%[36m 5 %ESC%[0m] web project - server side
::Echo    [%ESC%[36m 6 %ESC%[0m] web project - client side
Echo.
Echo    [%ESC%[36m rf %ESC%[0m] run dd-factory-frontend local server
Echo    [%ESC%[36m rt %ESC%[0m] run dd-tms-frontend local server
Echo.
Echo ===========================================================================
Echo    %ESC%[90mNote: all project will be placed in %WorkRoot% %ESC%[0m
Set Input=0918239021309810982093809
Set /P Input="-->"
if %Input% == 1 (
	Set Project=dd-mobile
	CALL :SET_WORKING_DIRS ddmobile
	GOTO :WM_000
)
if %Input% == 7 (
	Set Project=dd-loader
	CALL :SET_WORKING_DIRS ddloader
	GOTO :WM_600
)
::if %Input% == 2 (
::	Set Project=digital-dispatcher-tms
::	CALL :SET_WORKING_DIRS tms
::	GOTO :WM_100
::)
if %Input% == 3 (
	Set Project=dd-factory-backend
	CALL :SET_WORKING_DIRS ddfback
	GOTO :WM_200
)
if %Input% == 4 (
	Set Project=dd-factory-frontend
	CALL :SET_WORKING_DIRS ddffront
	GOTO :WM_300
)

if %Input% == 5 (
	Set Project=digital-dispatcher-tms
	CALL :SET_WORKING_DIRS ddtms
	GOTO :WM_400
)
if %Input% == 6 (
	Set Project=digital-dispatcher-web
	CALL :SET_WORKING_DIRS ddweb
	GOTO :WM_500
)

if %Input% == rf (
	start cmd /k "cd C:\Work\ddffront && npm run dev" 
	GOTO :PROJECT_SELECTION
)
if %Input% == rt (
	start cmd /k "cd C:\Work\ddtmsfront && npm run dev" 
	GOTO :PROJECT_SELECTION
)

Echo    %ESC%[41m Incorrect menu option selected, try again %ESC%[0m
pause
GOTO :PROJECT_SELECTION

:SET_WORKING_DIRS
Set Input=%1
:INPUT_WORK_DIR
Echo.
Echo    Input work directory for %ESC%[42m %Project% %ESC%[0m (%ESC%[90mby default %Input%%ESC%[0m):
Set /P Input="-->"
if %Input% == %1 (
	Echo    %ESC%[90mselected default work directory - %Input% %ESC%[0m
)

Set WorkDir=%Input%
Set WorkDirFull=%WorkRoot%\%WorkDir%
Set EnvFilesDir=%WorkRoot%\envFiles-%Project%

IF EXIST %WorkDirFull% (
	Echo    %ESC%[42m %WorkDirFull% is finded  %ESC%[0m
	pause
	cls
	EXIT /b
) ELSE (
	GOTO :WorkDir_NotExist
)
:WorkDir_NotExist
Echo.
Echo    %ESC%[41m Work directory %WorkDirFull% does not exist, create it? %ESC%[0m
Echo    select option (1-Yes, 2-set another directory):
Set Option=172731267398
Set /P Option="-->"
IF %Option%==1 (
	md %WorkDirFull%
	Echo    %ESC%[42m Directory created!  %ESC%[0m
	pause
	cls
	EXIT /b
) ELSE IF %Option%==2 (
	Set Input=%1
	GOTO :INPUT_WORK_DIR
) ELSE (
	Echo    %ESC%[41m Incorrect answer  %ESC%[0m
	GOTO :WorkDir_NotExist
)
EXIT /b

::GIT_CLONE ACTION===========================================================================
:GIT_CLONE
:WM_601
:WM_501
:WM_401
:WM_301
:WM_101
:WM_001
	Echo.
	Echo %JumpReturn%
	@Echo on
	cd %WorkDirFull%
	git clone https://github.com/prodimex/%Project% .
	@Echo off
	Echo    %ESC%[42m Repository cloning complete! %ESC%[0m
	GOTO %JumpReturn%
::END OF GIT_CLONE ACTION===================================================================

::COPY_ENV ACTION===========================================================================
:COPY_ENV
:WM_602
:WM_502
:WM_402
:WM_302
:WM_102
:WM_002
	Echo.
	Echo    Copy files from %EnvFilesDir% to %WorkDirFull%
	xcopy %EnvFilesDir%\* %WorkDirFull% /s /y
	Echo    %ESC%[42m Environment files copied %ESC%[0m 
	GOTO %JumpReturn%
::END OF COPY_ENV ACTION===================================================================

::JUMPER ACTION===========================================================================
	:JUMPER
	::Echo JUMP TO %JumpTo%
	::Echo GotoPref TO %GotoPref%
	::Echo JumpReturn TO %JumpReturn%
	::Echo JumpOption %JumpOption%
	::Echo WorkDir %WorkDir%
	IF %JumpOption% == * (
		GOTO :PROJECT_SELECTION
	)
	IF %JumpOption%==. (
		CALL :SET_WORKING_DIRS %WorkDir%
		GOTO %GotoPref%0
	)
	IF %JumpOption% == - Exit
	findstr /ri /c:"^ *%JumpTo% " /c:"^ *%JumpTo%$" "%~f0" >nul 2>nul && GOTO %JumpTo%
	Echo.
	Echo    %ESC%[41m Incorrect menu option selected  %ESC%[0m 
	Echo    Return to Worker Menu
	pause
	cls
	GOTO %JumpReturn%
::END OF JUMPER ACTION====================================================================

::SHOW_MENU_HEADER ACTION===========================================================================
	:SHOW_MENU_HEADER
	Echo.
	Echo                            --== Worker Menu ==--
	Echo ===========================================================================
	Echo        Project: %Project%
	EXIT /b
::END OF SHOW_MENU_HEADER ACTION====================================================================

::SHOW_MENU_BOTTOM ACTION===========================================================================
	:SHOW_MENU_BOTTOM
	Echo    [%ESC%[36m * %ESC%[0m] Change project
	Echo    [%ESC%[36m . %ESC%[0m] Change work directory
	Echo    [%ESC%[36m - %ESC%[0m] Exit worker
	Echo ===========================================================================
	EXIT /b
::END OF SHOW_MENU_BOTTOM ACTION====================================================================

::SHOW_OPTION_SELECTION ACTION===========================================================================
	:SHOW_OPTION_SELECTION
	CALL :SHOW_MENU_BOTTOM
	Echo    Select option what to do:
	Set GotoPref=%1
	Set JumpOption=61268376786986
	Set /P JumpOption="-->"
	Set JumpReturn=%GotoPref%0
	Set JumpTo=%GotoPref%%JumpOption%
	EXIT /b
	::EXIT /b
::END OF SHOW_OPTION_SELECTION ACTION====================================================================

:: ----------------------------------------------------------------------------------------------------------------------------------------------------
:WM_500
CALL :SHOW_MENU_HEADER
Echo    [%ESC%[36m 1 %ESC%[0m] Git clone to %WorkDirFull%
Echo    [%ESC%[36m 2 %ESC%[0m] Copy paste environment to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] Install node modules
Echo    [%ESC%[36m 4 %ESC%[0m] Started nuxt server
Echo   [%ESC%[36m ALL %ESC%[0m] Batch run all options inline
CALL :SHOW_OPTION_SELECTION :WM_50
GOTO :JUMPER

:WM_503
cd %WorkDirFull%
call npm install
Echo    %ESC%[42m Node modules installed %ESC%[0m 
GOTO %JumpReturn%

:WM_504
Echo %JumpReturn%
start cmd /k "cd %WorkDirFull% && npm run dev"
Echo    %ESC%[42m Nuxt server started %ESC%[0m
GOTO %JumpReturn%

:WM_50ALL
Set JumpReturn=:WM_50ALL_1
GOTO :WM_501

:WM_50ALL_1
Set JumpReturn=:WM_50ALL_2
GOTO :WM_502

:WM_50ALL_2
Set JumpReturn=:WM_50ALL_3
GOTO :WM_503

:WM_50ALL_3
Set JumpReturn=:WM_50ALL_4
GOTO :WM_504

:WM_50ALL_4
Echo    %ESC%[42m Batch action completed %ESC%[0m
GOTO :WM_500

:: ----------------------------------------------------------------------------------------------------------------------------------------------------
:WM_400
CALL :SHOW_MENU_HEADER
Echo    [%ESC%[36m 1 %ESC%[0m] Git clone to %WorkDirFull%
Echo    [%ESC%[36m 2 %ESC%[0m] Copy paste environment to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] Build and run docker containers
Echo    [%ESC%[36m 4 %ESC%[0m] Install dependencies
Echo    [%ESC%[36m 5 %ESC%[0m] Generate application key
Echo    [%ESC%[36m 6 %ESC%[0m] Create simlink for storage
Echo    [%ESC%[36m 7 %ESC%[0m] Run migrations and seeds
Echo    [%ESC%[36m 8 %ESC%[0m] Generate Laravel Passport tokens
Echo   [%ESC%[36m ALL %ESC%[0m] Batch run all options inline
CALL :SHOW_OPTION_SELECTION :WM_40
GOTO :JUMPER


:: ----------------------------------------------------------------------------------------------------------------------------------------------------
:WM_300
CALL :SHOW_MENU_HEADER
Echo    [%ESC%[36m 1 %ESC%[0m] Git clone to %WorkDirFull%
Echo    [%ESC%[36m 2 %ESC%[0m] Copy paste environment to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] Build and run docker containers
Echo   [%ESC%[36m ALL %ESC%[0m] Batch run all options inline
CALL :SHOW_MENU_BOTTOM
Echo    Select option what to do:
Set /P JumpOption="-->"
Set JumpReturn=:WM_300
Set JumpTo=:WM_30%JumpOption%
GOTO :JUMPER

:WM_30ALL
Echo "------------+++++"
Set JumpReturn=:WM_30ALL_1
GOTO :WM_301
:WM_30ALL_1
Echo "------------+++++"
Set JumpReturn=:WM_30ALL_2
GOTO :WM_302
:WM_30ALL_2
Echo "------------+++++"
Set JumpReturn=:WM_30ALL_3
GOTO :WM_303
:WM_30ALL_3
Echo "------------+++++"
Echo    %ESC%[42m Batch action completed %ESC%[0m
GOTO :WM_300

:: ----------------------------------------------------------------------------------------------------------------------------------------------------
:WM_100
:WM_200
CALL :SHOW_MENU_HEADER
Echo    [%ESC%[36m 1 %ESC%[0m] Git clone to %WorkDirFull%
Echo    [%ESC%[36m 2 %ESC%[0m] Copy paste environment to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] Build and run docker containers
Echo    [%ESC%[36m 4 %ESC%[0m] Install dependencies
Echo    [%ESC%[36m 5 %ESC%[0m] Generate application key
Echo    [%ESC%[36m 6 %ESC%[0m] Create simlink for storage
Echo    [%ESC%[36m 7 %ESC%[0m] Run migrations and seeds
Echo    [%ESC%[36m 8 %ESC%[0m] Generate Laravel Passport tokens

IF %WorkDir%==ddfback (
	Echo    [%ESC%[36m 9 %ESC%[0m] Add Administrator user for database - db
	Echo   [%ESC%[36m 10 %ESC%[0m] Add dev execution rights to Administrator - user
)
Echo   [%ESC%[36m ALL %ESC%[0m] Batch run all options inline
CALL :SHOW_MENU_BOTTOM
Echo    Select option what to do:
Set /P JumpOption="-->"
Set JumpReturn=:WM_100
Set JumpTo=:WM_10%JumpOption%
GOTO :JUMPER

:WM_103
:WM_203
:WM_303
:WM_403
cd %WorkDirFull%
docker-compose up -d
Echo    %ESC%[42m Docker containers builded and runned! %ESC%[0m 
GOTO %JumpReturn%

:WM_104
:WM_204
:WM_404
cd %WorkDirFull%
docker-compose exec php composer install
Echo    %ESC%[42m Project dependencies installed %ESC%[0m 
GOTO %JumpReturn%

:WM_105
:WM_205
:WM_405
cd %WorkDirFull%
docker-compose exec php php artisan key:generate
Echo    %ESC%[42m Application key generated %ESC%[0m 
GOTO %JumpReturn%

:WM_106
:WM_206
:WM_406
cd %WorkDirFull%
docker-compose exec php php artisan storage:link
Echo    %ESC%[42m Migrations and seeds completed %ESC%[0m 
GOTO %JumpReturn%

:WM_107
:WM_207
:WM_407
cd %WorkDirFull%
docker-compose exec php php artisan migrate --seed
Echo    %ESC%[42m Migrations and seeds completed %ESC%[0m 
GOTO %JumpReturn%

:WM_108
:WM_208
:WM_408
cd %WorkDirFull%
docker-compose exec php php artisan passport:install
Echo    %ESC%[42m Laravel Passport tokens generated %ESC%[0m 
GOTO %JumpReturn%

:WM_109
:WM_209
cd %WorkDirFull%
docker-compose exec php php artisan db:seed --class=UsersTableSeeder
Echo    %ESC%[42m Administrator has been added %ESC%[0m 
GOTO %JumpReturn%

:WM_1010
:WM_2010
cd %WorkDirFull%
git update-index --chmod=+x dev
Echo    %ESC%[42m dev execution added to db %ESC%[0m 
GOTO %JumpReturn%

:: 100 BATCH ALL ===========================================================================
:WM_10ALL
:WM_20ALL
:WM_40ALL
Set JumpReturn=:WM_10ALL_1
GOTO :WM_101
:WM_10ALL_1
Set JumpReturn=:WM_10ALL_2
GOTO :WM_102
:WM_10ALL_2
Set JumpReturn=:WM_10ALL_3
GOTO :WM_103
:WM_10ALL_3
Set JumpReturn=:WM_10ALL_4
GOTO :WM_104
:WM_10ALL_4
Set JumpReturn=:WM_10ALL_5
GOTO :WM_105
:WM_10ALL_5
Set JumpReturn=:WM_10ALL_6
GOTO :WM_106
:WM_10ALL_6
Set JumpReturn=:WM_10ALL_7
GOTO :WM_107
:WM_10ALL_7
IF %WorkDir%==ddfback (
	Set JumpReturn=:WM_20ALL_8
) ELSE (
	Set JumpReturn=:WM_10ALL_8
)
GOTO :WM_108
:WM_10ALL_8
Echo    %ESC%[42m Batch action completed %ESC%[0m
GOTO :WM_100

:WM_20ALL_8
Set JumpReturn=:WM_20ALL_9
GOTO :WM_209
:WM_20ALL_9
Set JumpReturn=:WM_10ALL_8
GOTO :WM_2010

:: 100 END OF BATCH ALL ===========================================================================

:: ----------------------------------------------------------------------------------------------------------------------------------------------------
:WM_600
CALL :SHOW_MENU_HEADER
Echo    [%ESC%[36m 1 %ESC%[0m] Git clone to %WorkDirFull%
Echo    [%ESC%[36m 2 %ESC%[0m] Copy paste:
Echo        .env, gradle.properties, keystores and google-services
Echo        to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] @react-native-community/cli install to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] npm install to %WorkDirFull%
Echo    [%ESC%[36m 4 %ESC%[0m] npx react-native run-android
Echo [%ESC%[36m 1234 %ESC%[0m] Batch run 1, 2, 3 and 4 options inline
Echo    [%ESC%[36m 5 %ESC%[0m] Assemble debug compilation
Echo   [%ESC%[36m 51 %ESC%[0m] Assemble release compilation
Echo    [%ESC%[36m 6 %ESC%[0m] Prepare apk's for upload
Echo    [%ESC%[36m 8 %ESC%[0m] Clean gradlew
CALL :SHOW_OPTION_SELECTION :WM_60
GOTO :JUMPER

:: ----------------------------------------------------------------------------------------------------------------------------------------------------
:WM_000
CALL :SHOW_MENU_HEADER
Echo    [%ESC%[36m 1 %ESC%[0m] Git clone to %WorkDirFull%
Echo    [%ESC%[36m 2 %ESC%[0m] Copy paste:
Echo        .env, gradle.properties, keystores and google-services
Echo        to %WorkDirFull%
Echo    [%ESC%[36m 3 %ESC%[0m] npm install to %WorkDirFull%
Echo    [%ESC%[36m 4 %ESC%[0m] npx react-native run-android
Echo   [%ESC%[36m 41 %ESC%[0m] npx react-native run-android --variant=debug
Echo   [%ESC%[36m 42 %ESC%[0m] npx react-native run-android --variant=release //not working
Echo.
Echo [%ESC%[36m 1234 %ESC%[0m] Batch run 1, 2, 3 and 4 options inline
Echo.
Echo    [%ESC%[36m 5 %ESC%[0m] Assemble debug compilation
Echo   [%ESC%[36m 51 %ESC%[0m] Assemble release compilation
Echo   [%ESC%[36m 52 %ESC%[0m] Bundle debug compilation //not realized
Echo   [%ESC%[36m 53 %ESC%[0m] Bundle release compilation //not realized
Echo.
Echo    [%ESC%[36m 6 %ESC%[0m] Prepare apk's for upload
Echo    [%ESC%[36m 7 %ESC%[0m] Start React Native server
Echo    [%ESC%[36m 8 %ESC%[0m] Clean gradlew
Echo    [%ESC%[36m 9 %ESC%[0m] Run custom command
CALL :SHOW_MENU_BOTTOM
Echo    Select option what to do:
Set /P JumpOption="-->"
Set JumpReturn=:WM_000
Set JumpTo=:WM_00%JumpOption%
GOTO :JUMPER

:WM_603
:WM_003
Echo.
start cmd /k "cd %WorkDirFull% && npm install"
Echo    %ESC%[94m External command is started, wayt for result. %ESC%[0m
GOTO %JumpReturn%

:WM_604
:WM_004
start cmd /k "cd %WorkDirFull% && npx react-native run-android"
Echo    %ESC%[94m External command is started, wayt for result. %ESC%[0m
GOTO %JumpReturn%

:WM_601234
:WM_001234
Echo.
@Echo on
cd %WorkDirFull%
git clone https://github.com/prodimex/%Project% .
@Echo off

Echo    Copy files from %EnvFilesDir% to %WorkDirFull%
xcopy %EnvFilesDir%\* %WorkDirFull% /s /y
Echo    %ESC%[42m Environment files copied  %ESC%[0m 

start cmd /k "cd %WorkDirFull% && npm install && npx react-native run-android"
Echo    %ESC%[94mExternal command is started, wayt for result. %ESC%[0m
GOTO %JumpReturn%



:WM_0041
start cmd /k "cd %WorkDirFull% && npx react-native run-android --variant=debug"
Echo    %ESC%[94mExternal command is started, wayt for result. %ESC%[0m
GOTO :WM_000

:WM_0042
start cmd /k "cd %WorkDirFull% && npx react-native run-android --variant=release"
Echo    %ESC%[94mExternal command is started, wayt for result. %ESC%[0m
GOTO :WM_000

:WM_605
:WM_005
start cmd /k "cd %WorkDirFull%/android && gradlew assembleDebug"
GOTO %JumpReturn%

:WM_6051
:WM_0051
start cmd /k "cd %WorkDirFull% && npx react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle --assets-dest android/app/src/main/res && cd %WorkDirFull%/android && gradlew clean && gradlew assembleRelease -x bundleReleaseJsAndAssets"
GOTO %JumpReturn%

:WM_606
:WM_006
Echo    Input version name:
Set /P VersionName="-->"
CALL :COPY_TO_UPLOAD %VersionName% debug
CALL :COPY_TO_UPLOAD %VersionName% release
Echo    %ESC%[42m End of prepare to upload  %ESC%[0m
GOTO %JumpReturn%

:COPY_TO_UPLOAD 
	Set VersionName=%1
	Set Version=%2
	Set FileURI=%WorkDirFull%\android\app\build\outputs\apk\%Version%\app-%Version%.apk
	Set TargetDir=%WorkDirFull%\update\releases
	Set NewFileName=%VersionName%-%Version%.apk
	IF EXIST %FileURI% (
		Echo    "   Copy file from %FileURI%"
		Echo    "   to %TargetDir%"
		Echo    "   as %NewFileName%"
		copy /y "%FileURI%" "%TargetDir%\%NewFileName%"
	) ELSE (
		Echo    %ESC%[43m %Version% apk version not compiled  %ESC%[0m
	)
EXIT /b

:WM_007
start cmd /k "cd %WorkDirFull% && npx react-native start"
GOTO :WM_000

:WM_608
:WM_008
start cmd /k "cd %WorkDirFull%\android && gradlew clean"
GOTO :WM_000

:WM_009
Echo.
Echo    Command:
Set /P CustomCommand="-->"
%CustomCommand%
GOTO :WM_000

:WM_-
EXIT
pause

:setESC
for /F "tokens=1,2 delims=#" %%a in ('"prompt #$H#$E# & echo on & for %%b in (1) do rem"') do (
  set ESC=%%b
  exit /b
)
