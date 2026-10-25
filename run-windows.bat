@echo off
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven is not installed or not on PATH.
  echo Install Apache Maven, then run: mvn spring-boot:run
  pause
  exit /b 1
)
mvn spring-boot:run
pause
