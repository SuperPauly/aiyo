# Research: Remote OpenCode Integration via SSH Tunnel & API

## 1. Overview
The goal is to enable **Aiyo** to chat with a remote **OpenCode** agent running on a user's VPS.
*   **User Experience:** Chat bubbles (no terminal).
*   **Connectivity:** SSH Tunnel to VPS.
*   **Protocol:** HTTP + Server-Sent Events (SSE) talking to `opencode serve`.

## 2. Architecture: "The Tunnel & Client"
Instead of scraping a CLI, we will use OpenCode's native API.

### 2.1 The Setup (User Side)
*   User runs `opencode serve` on their VPS.
*   Server listens on `127.0.0.1:4096` (default).
*   No public port is exposed.

### 2.2 The Connection (App Side)
1.  **SSH Connection:**
    *   Aiyo connects to VPS via SSH (JSch).
    *   **Port Forwarding:** Aiyo forwards a local Android port (e.g., `localhost:5555`) to remote `localhost:4096`.
2.  **API Client:**
    *   Aiyo's HTTP Client (Retrofit/OkHttp) talks to `http://localhost:5555`.
    *   This traffic travels securely through the SSH tunnel.

### 2.3 The Protocol (OpenCode API)
*   **Create Session:** `POST /session` -> returns `sessionId`.
*   **Send Message:** `POST /session/{id}/message` (Body: `{ "content": "Hello" }`).
*   **Stream Events:** `GET /event` (SSE).
    *   Events: `server.connected`, `delta` (text chunks), `status` (tool use).
    *   *Note:* Need to filter events by `sessionId` if the server is shared (though likely single-user for this MVP).

## 3. Implementation Plan Refinement

### 3.1 Dependencies
*   **SSH:** `jsch` (for tunnel).
*   **HTTP:** `retrofit`, `okhttp-sse` (for API & events).

### 3.2 Data Layer (`:data`)
*   `SshTunnelService`: Manages the JSch session and port forwarding.
    *   `startTunnel(config: SshConfig): Int` (returns local port).
    *   `stopTunnel()`.
*   `OpenCodeApi`: Retrofit interface.
*   `OpenCodeRepository`:
    *   Coordinates the tunnel startup.
    *   Creates the Retrofit client pointing to the forwarded port.
    *   Exposes `sendMessage` and `eventsFlow`.

### 3.3 UI Layer (`:ui`)
*   **ChatViewModel:**
    *   Connects to `OpenCodeRepository`.
    *   Maps SSE events to `Message` updates.
    *   Handles "Connecting..." state while tunnel comes up.

## 4. Risks & Mitigations
*   **Port Conflicts:** Local port 5555 might be taken.
    *   *Mitigation:* Dynamically find a free local port or try a range.
*   **Server Not Running:**
    *   *Mitigation:* If API fails, try to run `opencode serve` via SSH `exec` channel automatically (advanced) or just error out telling user to run it.
*   **Process Death:**
    *   Tunnel dies if app is killed. Chat needs to reconnect/re-tunnel on resume.

## 5. Conclusion
This approach is robust, secure, and uses the "correct" API surface of OpenCode while maintaining the easy setup of "just give me your SSH credentials".