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

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

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

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\compiler-cli-1.0.0.jar;%APP_HOME%\lib\compiler-sdk-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-packaging-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-optimizer-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-generator-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-reporting-1.0.0.jar;%APP_HOME%\lib\compiler-contracts-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-workflow-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-projection-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-blueprint-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-audit-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-dsl-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\compiler-core-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\runtime-plugin-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\runtime-event-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\runtime-module-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\runtime-config-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\micronaut-runtime-4.5.0.jar;%APP_HOME%\lib\micronaut-discovery-core-4.5.0.jar;%APP_HOME%\lib\micronaut-http-4.5.0.jar;%APP_HOME%\lib\micronaut-context-propagation-4.5.0.jar;%APP_HOME%\lib\micronaut-retry-4.5.0.jar;%APP_HOME%\lib\micronaut-context-4.5.0.jar;%APP_HOME%\lib\micronaut-aop-4.5.0.jar;%APP_HOME%\lib\micronaut-inject-4.5.0.jar;%APP_HOME%\lib\runtime-observability-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\runtime-core-1.0.0.jar;%APP_HOME%\lib\jakarta.inject-api-2.0.1.jar;%APP_HOME%\lib\picocli-4.7.6.jar;%APP_HOME%\lib\jackson-module-jakarta-xmlbind-annotations-2.17.2.jar;%APP_HOME%\lib\jackson-core-2.17.2.jar;%APP_HOME%\lib\jackson-annotations-2.17.2.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.17.2.jar;%APP_HOME%\lib\logstash-logback-encoder-7.4.jar;%APP_HOME%\lib\jackson-databind-2.17.2.jar;%APP_HOME%\lib\slf4j-simple-2.0.17.jar;%APP_HOME%\lib\micronaut-core-reactive-4.5.0.jar;%APP_HOME%\lib\micronaut-core-4.5.0.jar;%APP_HOME%\lib\logback-classic-1.5.6.jar;%APP_HOME%\lib\slf4j-api-2.0.17.jar;%APP_HOME%\lib\jakarta.annotation-api-2.1.1.jar;%APP_HOME%\lib\reactor-core-3.5.11.jar;%APP_HOME%\lib\asm-9.6.jar;%APP_HOME%\lib\jakarta.xml.bind-api-3.0.1.jar;%APP_HOME%\lib\jakarta.activation-api-2.1.0.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\guice-5.1.0.jar;%APP_HOME%\lib\opentelemetry-exporter-prometheus-1.35.0-alpha.jar;%APP_HOME%\lib\opentelemetry-sdk-extension-autoconfigure-spi-1.35.0.jar;%APP_HOME%\lib\opentelemetry-sdk-1.35.0.jar;%APP_HOME%\lib\opentelemetry-sdk-metrics-1.35.0.jar;%APP_HOME%\lib\opentelemetry-sdk-trace-1.35.0.jar;%APP_HOME%\lib\opentelemetry-sdk-logs-1.35.0.jar;%APP_HOME%\lib\opentelemetry-sdk-common-1.35.0.jar;%APP_HOME%\lib\opentelemetry-extension-incubator-1.35.0-alpha.jar;%APP_HOME%\lib\opentelemetry-api-events-1.35.0-alpha.jar;%APP_HOME%\lib\opentelemetry-api-1.35.0.jar;%APP_HOME%\lib\jakarta.activation-2.0.1.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\guava-30.1-jre.jar;%APP_HOME%\lib\opentelemetry-context-1.35.0.jar;%APP_HOME%\lib\prometheus-metrics-exporter-httpserver-1.1.0.jar;%APP_HOME%\lib\logback-core-1.5.6.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.5.0.jar;%APP_HOME%\lib\error_prone_annotations-2.3.4.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\prometheus-metrics-exporter-common-1.1.0.jar;%APP_HOME%\lib\prometheus-metrics-exposition-formats-1.1.0.jar;%APP_HOME%\lib\prometheus-metrics-model-1.1.0.jar;%APP_HOME%\lib\prometheus-metrics-config-1.1.0.jar;%APP_HOME%\lib\prometheus-metrics-shaded-protobuf-1.1.0.jar


@rem Execute devinebyte
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %DEVINEBYTE_OPTS%  -classpath "%CLASSPATH%" io.devinebyte.compiler.cli.CompilerCli %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable DEVINEBYTE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%DEVINEBYTE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
