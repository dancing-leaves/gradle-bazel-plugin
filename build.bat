::cmd

:: script to test build quickly
:: adjust to your environment

@echo off
setlocal

set PROJECT_DIR=%~dp0
set TOOLS_DIR=%~dp0..\tools

set BAZEL_VC=C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\VC

set PATH=^
%TOOLS_DIR%;^
%TOOLS_DIR%\gradle-5.3.1-all\bin;^
%TOOLS_DIR%\jdk1.8.0_202\bin;^
%systemroot%\System32

pushd "%PROJECT_DIR%"
rmdir /s/q build .gradle 2>nul
cmd /c gradle --info build

endlocal & cmd /c exit %errorlevel%