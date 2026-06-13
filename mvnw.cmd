@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __ MVNW_CMD__=%COMSPEC%
@IF NOT "%COMSPEC%"=="" (SET __MVNW_CMD__=%COMSPEC%)
@IF "%__MVNW_CMD__%"=="" (SET __MVNW_CMD__=cmd.exe)

@SETLOCAL
@SET DIRNAME=%~dp0
@IF "%DIRNAME%"=="" (SET DIRNAME=.\)
@SET APP_BASE_NAME=%~n0
@SET APP_HOME=%DIRNAME%

@REM Resolve any "." and ".." in APP_HOME to make it shorter.
@FOR %%i IN ("%APP_HOME%") DO @SET APP_HOME=%%~fi

@SET MAVEN_PROJECTBASEDIR=%APP_HOME%
@CALL :find_maven_basedir "%APP_HOME%"
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO done_basedir
@IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn" GOTO done_basedir
:find_maven_basedir
@SET MAVEN_PROJECTBASEDIR=%~1
:done_basedir

@REM Download Maven wrapper jar if not present
@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@IF NOT EXIST %WRAPPER_JAR% (
    @ECHO Downloading Maven Wrapper...
    @IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" (
        @MKDIR "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
    )
    @powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile(%DOWNLOAD_URL%, %WRAPPER_JAR%) }"
)

@SET JAVA_EXE=java.exe
@IF NOT "%JAVA_HOME%"=="" (
    @SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"
)

@SET WRAPPER_LAUNCHER_ARGS=-Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%"

@%JAVA_EXE% %WRAPPER_LAUNCHER_ARGS% ^
  -classpath %WRAPPER_JAR% ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

@IF "%ERRORLEVEL%"=="0" (GOTO end)
@EXIT /B %ERRORLEVEL%
:end
@ENDLOCAL
