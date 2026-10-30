@echo off
rem Start backend without worrying about PowerShell execution policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev-backend.ps1" %*
