@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "SPEC_FILE=sim-api-soatest-openapi.yaml"
set "PORT=8090"

echo Starting Swagger UI on http://localhost:%PORT%
echo Using spec: %SCRIPT_DIR%%SPEC_FILE%
echo Press Ctrl+C to stop.

docker run --rm -p %PORT%:8080 -e SWAGGER_JSON=/spec/%SPEC_FILE% -v "%SCRIPT_DIR%:/spec" swaggerapi/swagger-ui
