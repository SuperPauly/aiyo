# OpenCode Docker Server for Aiyo

This directory contains the setup to run an OpenCode agent on your VPS, which Aiyo can connect to via SSH.

## Prerequisites
*   Docker installed on your VPS.
*   An OpenCode auth file (`auth.json`) containing your LLM API keys. You can generate this by running `opencode auth login` on your local machine and copying the file.

## Setup Instructions

1.  **Build the Image:**
    ```bash
    docker build -t opencode-server .
    ```

2.  **Run the Container:**
    You need to mount your `auth.json` and the directory you want the agent to work in.

    ```bash
    # Create a config directory if it doesn't exist
    mkdir -p ~/.config/opencode

    # Copy your auth.json to ~/.config/opencode/auth.json on the VPS first!

    docker run -d \
      --name opencode-agent \
      --restart unless-stopped \
      -p 127.0.0.1:4096:4096 \
      -v ~/.config/opencode/auth.json:/home/opencodeuser/.local/share/opencode/auth.json \
      -v $(pwd)/workspace:/home/opencodeuser/app \
      opencode-server
    ```

    *   `-p 127.0.0.1:4096:4096`: Binds the server to **localhost only** on the VPS. This is critical for security; Aiyo will access it via a secure SSH tunnel. Do **not** expose it to `0.0.0.0` unless you have a firewall.
    *   `-v ...auth.json`: Mounts your credentials.
    *   `-v ...workspace`: Mounts the folder where you want OpenCode to write files.

3.  **Connect from Aiyo:**
    *   In Aiyo Settings, go to "SSH Agent Configuration".
    *   Enter your VPS Host, Port (22), Username, and Private Key.
    *   Start a new chat and select "SSH Agent" as the model.

