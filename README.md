# Ping Nametag

Fabric client mod that appends player latency to nametags and keeps it persistent even when servers customize nametags (rank prefixes/suffixes).

## Features

- Ping value shown on every player nametag (`(123ms)`)
- Color-coded ping tiers (good / medium / bad / worst)
- Cloth Config powered settings screen (via Mod Menu)
- Overlay is injected at final render time so it stays visible alongside server-edited nametag text

## Minecraft / Fabric version

This project is configured for **Fabric 1.21.11**.

## Build

```bash
./gradlew build
```

Build output jar is generated in `build/libs/`.
