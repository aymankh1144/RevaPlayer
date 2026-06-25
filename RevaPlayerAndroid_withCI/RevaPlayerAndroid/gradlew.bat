@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0

@rem Execute Gradle
"%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" %*
