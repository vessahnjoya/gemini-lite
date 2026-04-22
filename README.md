# Gemini-Lite

A Java implementation of the **Gemini-Lite protocol** — a simplified, lightweight alternative to HTTP designed for minimal overhead and simplicity. This project includes a fully functional **Client**, **Server**, and **Proxy**, along with a benchmarking tool for performance analysis.

> Built as part of the BCS2110 course at Maastricht University.

---

## What is Gemini-Lite?

Gemini-Lite is a streamlined network protocol that operates over TCP. Unlike HTTP, it uses a single request/response per connection with a minimal header format:

- **Request:** `<absolute-URI>\r\n` (max 1024 bytes)
- **Response:** `<STATUS> <META>\r\n[<BODY>]`
  - Status codes are 2 digits (10–59)
  - Body is only present for `2x` (Success) responses

**Status Code Classes:**

| Code | Meaning            | Description                          |
|------|--------------------|--------------------------------------|
| 1x   | Input              | Server requests additional input     |
| 2x   | Success            | Response body follows                |
| 3x   | Redirect           | META contains the redirect URL       |
| 4x   | Temporary Failure  | Retry later                          |
| 5x   | Permanent Failure  | Do not retry                         |

---

## Features

### Client

- Fetches resources from Gemini-Lite servers
- Follows redirects (up to 5 hops)
- Handles input requests (status `1x`) by prompting for user input
- Implements **exponential backoff** for slowdown responses (status `44`), capped at 60s
- Streams lar3ge files efficiently without loading into memory

### Server

- Serves files from a configurable document root
- Automatic MIME type detection (`.gmi` → `text/gemini`, `.txt` → `text/plain`)
- Generates directory listings in Gemtext format
- **Path traversal protection** — prevents access outside the document root
- Proper error responses for missing files and server errors

### Proxy

- Transparent intermediary between clients and origin servers
- Follows redirects on behalf of the client
- Relay complete responses including bodies
- Enforces redirect limits to prevent infinite loops

### Benchmark

- Measures **p50** and **p99** latencies across multiple file sizes (64B to 100MB)
- Reports throughput for large file transfers
- Runs 20 iterations per test for statistical reliability

---

## Tech Stack

- **Language:** Java 23+
- **Build Tool:** Maven
- **Testing:** JUnit 5

---

## Getting Started

### Prerequisites

- Java JDK 23 or later
- Apache Maven 3.9+

### Build

```bash
mvn clean package
```

This produces `target/bcs2110-2025.jar`.

### Run the Server

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Server [docRoot] [port]
```

**Example:**

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Server docroot 1958
```

- `docRoot` — directory to serve files from (default: current directory)
- `port` — port to listen on (default: `1958`)

### Run the Client

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Client <URL> [input]
```

**Example:**

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost/index.gmi
```

### Run the Proxy

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Proxy [port]
```

**Example:**

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Proxy 1959
```

Then point the client through the proxy:

```bash
java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost:1959/index.gmi
```

### Run Tests

```bash
mvn test
```

---

## Project Structure

```text
src/main/java/
├── gemini_lite/
│   ├── Client.java          // CLI client entry point
│   ├── Server.java          // CLI server entry point
│   └── Proxy.java           // CLI proxy entry point
├── engine/
│   ├── ClientEngine.java    // Core client logic (fetch, retry, redirect)
│   ├── ServerEngine.java    // Core server logic (routing, response)
│   └── ProxyEngine.java     // Core proxy logic (relay, redirect)
├── handler/
│   ├── ResourceHandler.java   // Interface for serving resources
│   ├── FilesystemHandler.java // Filesystem-based resource handler
│   └── ReplyAndBody.java      // Reply record with body data
└── protocol/
    ├── Request.java           // Request parsing and validation
    ├── Reply.java             // Response parsing and validation
    └── ProtocolException.java // Custom exception for protocol errors
```

---

## Architecture

```text
┌────────────────────────────────────────────────────────┐
│                   CLI Entry Points                     │
│        Client.java  │  Server.java  │  Proxy.java      │
└───────────┬─────────┴───────┬───────┴────────┬─────────┘
            │                 │                │
┌───────────▼─────┐ ┌────────▼────────┐ ┌─────▼──────────┐
│  ClientEngine   │ │  ServerEngine   │ │  ProxyEngine   │
│  - Fetch/retry  │ │  - Parse req    │ │  - Relay req   │
│  - Redirects    │ │  - Route/serve  │ │  - Redirects   │
│  - Backoff      │ │  - MIME detect  │ │  - Error relay │
└───────────┬─────┘ └────────┬────────┘ └─────┬──────────┘
            │                │                │
            └────────┬───────┴────────┬───────┘
                     │                │
           ┌─────────▼──────┐ ┌──────▼──────────────┐
           │ Protocol Layer │ │  Resource Handler    │
           │ Request/Reply  │ │ (FilesystemHandler)  │
           │ parsing &      │ │  - Safe path resolve │
           │ validation     │ │  - Directory listing │
           └────────────────┘ └─────────────────────┘
```

---

## Performance

## License

This project was developed for educational purposes as part of the BCS2110 course at Maastricht University.
