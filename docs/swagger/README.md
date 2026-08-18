# SIM Swagger/OpenAPI Artifacts

This folder contains generated API inventory and a starter OpenAPI document.

## Browser UI Testing

You can launch Swagger UI locally and test endpoints from a browser.

1. Run `run-swagger-ui.cmd` from this folder.
2. Open `http://localhost:8090`.
3. Use `Try it out` on endpoints.
4. Authenticate first with `POST /api/auth/login`, then reuse the returned `JSESSIONID` cookie for protected endpoints.

## Files

- `endpoint_inventory.csv`
  - Source-derived servlet endpoint inventory from `@WebServlet` annotations.
  - Includes URL path and HTTP methods detected from `doGet/doPost/doPut/doDelete`.

- `sim-api-soatest-openapi.yaml`
  - Starter OpenAPI 3.0 spec for API-style endpoints.
  - Includes `/api/auth/login` (JAX-RS) with concrete request/response schema.
  - Includes selected servlet endpoints with generic request/response placeholders.

- `run-swagger-ui.cmd`
  - Launches Swagger UI in Docker and loads `sim-api-soatest-openapi.yaml` automatically.

## Assumptions

- API context path: `/chat-server`
- Full base URL depends on environment host (for example: `https://heavyarms/chat-server`)

## How To Use In SOAtest

1. Import OpenAPI: `sim-api-soatest-openapi.yaml`.
2. Set environment base URL to your deployed host/context.
3. Create an authentication test using `POST /api/auth/login` with JSON body `{ "username": "...", "password": "..." }`.
4. If your environment uses session auth, capture the login cookie and apply it to protected endpoint tests.
5. Refine endpoint request bodies and response assertions incrementally as you validate real payloads.

## Next Refinement Pass (Recommended)

- Refine remaining generic endpoints as needed:
  - `/admin/save-config`
  - `/admin/test-salesforce-connection`
  - `/admin/db/import`
  - `/dashboard/widgets/drilldown/*`
  - `/dashboard/latest-chats`
  - `/dashboard/new-users/*`
  - `/dashboard/topics/*`
  - `/dashboard/trends/*`
  - `/dashboard/sessions/chats`, `/dashboard/sessions/select`

## Completed Refinements

- `/admin/db/backup` now declares `application/zip` binary response.
- `/dashboard/summary-markdown` endpoint added with `text/html` response.
- Generic schemas replaced with concrete schemas for:
  - `/admin/test-connection` (`TestConnectionRequest`/`TestConnectionResponse`)
  - `/admin/widget-health-config` (`WidgetHealthConfig`)
  - `/dashboard/sessions/data` (`SessionDataResponse`/`SessionSummary`/`SessionSelectRequest`)
  - `/dashboard/widgets` (`WidgetListResponse`/`WidgetSingleResponse`/`WidgetDeleteResponse`/`WidgetRequest`/`WidgetEntry`)
