@echo off
rem Drop this file and mod-conflict-detector.jar into your Songs of Syx
rem install folder (the one containing SongsOfSyx.jar), then double-click
rem this file to scan your enabled mods.
java -jar "%~dp0mod-conflict-detector.jar" --game-dir "%~dp0"
echo.
pause
