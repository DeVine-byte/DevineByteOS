@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  devinebyte startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables, and ensure extensions are enabled
setlocal EnableExtensions

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and DEVINEBYTE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

"%COMSPEC%" /c exit 1

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\compiler-cli-1.0.0.jar;%APP_HOME%\lib\compiler-sdk.jar;%APP_HOME%\lib\compiler-packaging.jar;%APP_HOME%\lib\compiler-optimizer.jar;%APP_HOME%\lib\compiler-generator.jar;%APP_HOME%\lib\compiler-reporting-1.0.0.jar;%APP_HOME%\lib\compiler-contracts.jar;%APP_HOME%\lib\compiler-workflow.jar;%APP_HOME%\lib\compiler-projection.jar;%APP_HOME%\lib\compiler-blueprint.jar;%APP_HOME%\lib\compiler-audit.jar;%APP_HOME%\lib\compiler-dsl.jar;%APP_HOME%\lib\compiler-core.jar;%APP_HOME%\lib\runtime-config.jar;%APP_HOME%\lib\micronaut-runtime-4.5.0.jar;%APP_HOME%\lib\micronaut-discovery-core-4.5.0.jar;%APP_HOME%\lib\micronaut-http-4.5.0.jar;%APP_HOME%\lib\micronaut-context-propagation-4.5.0.jar;%APP_HOME%\lib\micronaut-retry-4.5.0.jar;%APP_HOME%\lib\micronaut-context-4.5.0.jar;%APP_HOME%\lib\micronaut-aop-4.5.0.jar;%APP_HOME%\lib\micronaut-inject-4.5.0.jar;%APP_HOME%\lib\runtime-core-1.0.0.jar;%APP_HOME%\lib\jakarta.inject-api-2.0.1.jar;%APP_HOME%\lib\picocli-4.7.6.jar;%APP_HOME%\lib\jackson-module-jakarta-xmlbind-annotations-2.17.2.jar;%APP_HOME%\lib\jackson-annotations-2.17.2.jar;%APP_HOME%\lib\jackson-core-2.17.2.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.17.2.jar;%APP_HOME%\lib\jackson-databind-2.17.2.jar;%APP_HOME%\lib\slf4j-simple-2.0.17.jar;%APP_HOME%\lib\micronaut-core-reactive-4.5.0.jar;%APP_HOME%\lib\micronaut-core-4.5.0.jar;%APP_HOME%\lib\slf4j-api-2.0.17.jar;%APP_HOME%\lib\jakarta.annotation-api-2.1.1.jar;%APP_HOME%\lib\reactor-core-3.5.11.jar;%APP_HOME%\lib\jakarta.xml.bind-api-3.0.1.jar;%APP_HOME%\lib\jakarta.activation-api-2.1.0.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\jakarta.activation-2.0.1.jar


@rem Execute devinebyte
@rem endlocal doesn't take effect until after the line is parsed and variables are expanded
@rem which allows us to clear the local environment before executing the java command
endlocal & "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %DEVINEBYTE_OPTS%  -classpath "%CLASSPATH%" io.devinebyte.compiler.cli.CompilerCli %* & call :exitWithErrorLevel

:exitWithErrorLevel
@rem Use "%COMSPEC%" /c exit to allow operators to work properly in scripts
"%COMSPEC%" /c exit %ERRORLEVEL%
