@echo off
chcp 65001 >nul
set JAR_FILE=%~dp0target\devcli-1.0.0.jar
if not exist "%JAR_FILE%" (
    echo Building DevCLI executable package...
    call mvn clean package -DskipTests -q
)
java -Dfile.encoding=UTF-8 -jar "%JAR_FILE%" %*
