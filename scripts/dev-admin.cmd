@echo off
rem Start admin dev server without worrying about PowerShell execution policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev-admin.ps1" %*
