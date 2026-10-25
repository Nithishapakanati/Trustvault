# TrustVault — Secure Document Sharing Prototype

A Spring Boot REST application and simple browser UI demonstrating document creation, read-only sharing, access revocation, and audit events. It uses an embedded H2 database so the demo does not require a local MySQL installation.

## Requirements
- Java 21 or newer
- Internet connection for the first Maven build (to download dependencies)

## Run on Windows PowerShell
1. Extract `TrustVault.zip` to a folder.
2. Open the extracted `trustvault` folder in VS Code.
3. In the terminal, run:

   ```powershell
   mvn spring-boot:run

   Alternatively, double-click `run-windows.bat` if Maven is installed and available on PATH.
   ```

4. Open `http://localhost:8080` in your browser.
5. Sign in with one of the demo accounts:
   - Owner: `owner` / `Owner@123`
   - Recipient: `viewer` / `Viewer@123`

The H2 database is stored in the project's `data` directory. The browser interface lets you create a text document, grant read access to the viewer, sign out and sign in as viewer to open it, and revoke access as owner.

## REST API
All API endpoints use HTTP Basic authentication.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/documents` | Create a document. JSON: `{"title":"Example","content":"Demo text"}` |
| GET | `/api/documents` | List documents owned by or shared with the signed-in user |
| GET | `/api/documents/{id}` | Read a document if owner or active recipient |
| POST | `/api/documents/{id}/share` | Grant READ access. JSON: `{"recipientUsername":"viewer"}` |
| DELETE | `/api/documents/{id}/share/{recipient}` | Revoke recipient access |
| GET | `/api/audit` | View audit events |

## Security and scope notes
- This is a student/demo prototype, not a production-ready secure storage service.
- Documents contain text in a local H2 database; binary file upload, malware scanning, external identity providers, encryption-at-rest key management, and rate limiting are not implemented.
- Demo users and passwords are hard-coded for local demonstration. Replace them with proper account management and secret storage before any real deployment.
- The audit endpoint is currently available to any authenticated demo user; production systems should enforce an administrator/owner policy.
- The application logs document creation, view attempts, grants, and revocations. Revocation blocks subsequent API reads through the permission check.

## Suggested threat model
- Unauthorized user guesses a document ID: server checks ownership and active sharing permission before returning content.
- Owner revokes a recipient: permission is marked inactive and subsequent reads are denied.
- User attempts to grant/revoke someone else's document: owner checks reject the operation.
- Audit trail review: events record actor, action, document ID, details, and timestamp.

## Suggested validation before submission
1. Run the application and confirm the UI loads.
2. Create a document as `owner`.
3. Share it with `viewer`.
4. Sign out, sign in as `viewer`, and open the shared document.
5. Sign in as `owner`, revoke `viewer` access, and verify that the viewer can no longer open it.
6. Review audit history.

Include a short explanation of what you tested and any remaining limitations in your submission. Do not claim unimplemented features are complete.
