# Plan: Remote OpenCode Integration (SSH Tunnel + API)

## Goal
Enable users to chat with an OpenCode agent running on their VPS by tunneling the OpenCode API over SSH and consuming it via HTTP/SSE.

## Architecture
*   **Transport:** SSH Local Port Forwarding (JSch).
*   **Protocol:** HTTP + SSE (Retrofit + OkHttp) talking to `localhost:forwarded_port`.
*   **UI:** Native Chat Bubbles.

## Milestones

### 1. Dependencies & Domain Setup
*   **Objective:** Prepare project for SSH and SSE.
*   **Tasks:**
    *   [ ] Add `jsch` to `:data` dependencies.
    *   [ ] Add `okhttp-sse` to `:data` dependencies.
    *   [ ] Create `SshConfig` in `:domain` (host, port, user, auth).
    *   [ ] Define `RemoteAgentSession` interface in `:domain`.

### 2. SSH Tunnel Implementation
*   **Objective:** Create a secure pipe to the remote server.
*   **Tasks:**
    *   [ ] Create `SshTunnelManager` in `:data`.
        *   `connect(config)`: Establishes SSH session.
        *   `startForwarding(remotePort: Int): Int`: Forwards random local port to remote port (default 4096).
        *   `disconnect()`: Cleans up.
    *   [ ] Add settings UI for SSH credentials (as per previous plan).

### 3. OpenCode API Client
*   **Objective:** Talk to the OpenCode server.
*   **Tasks:**
    *   [ ] Create `OpenCodeApi` Retrofit interface.
        *   `POST /session`
        *   `POST /session/{id}/message`
    *   [ ] Create `OpenCodeEventSource` (using OkHttp SSE).
        *   Connects to `GET /event`.
        *   Emits typed events (TextDelta, ToolStart, ToolEnd).
    *   [ ] Create `OpenCodeRepository` (implements `RemoteAgentSession`).
        *   Orchestrates: Start Tunnel -> Build Client -> Connect API.

### 4. Chat UI Integration
*   **Objective:** Display the stream.
*   **Tasks:**
    *   [ ] Update `ChatViewModel`.
        *   Handle `SSH_AGENT` model type.
        *   On init: Start tunnel & session.
        *   On send: `repo.sendMessage()`.
        *   On event: Update message list (append text, show status).
    *   [ ] Handle connection lifecycle (reconnect on resume if needed).

### 5. Polish
*   **Objective:** User experience.
*   **Tasks:**
    *   [ ] Error handling: "Ensure 'opencode serve' is running on the server".
    *   [ ] Dynamic port finding for the tunnel.

## User Verification
*   **Constraint:** User must run `opencode serve` on the VPS manually (or we document it).
