@echo off
rem Start mobile dev server without worrying about PowerShell execution policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev-mobile.ps1" %*
