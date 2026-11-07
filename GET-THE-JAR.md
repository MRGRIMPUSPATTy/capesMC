# Getting your capesMC .jar (no setup on your PC)

Minecraft only loads .jar files, so this zip (source code) will NOT show up in your mods folder.
GitHub can build the jar for you for free:

1. Make a free account at github.com, click New repository, name it capesMC, create it.
2. Click "uploading an existing file", drag in EVERYTHING from this folder
   (including the hidden .github folder) and commit.
3. Open the Actions tab. A "Build jar" run starts by itself (about 3-5 minutes).
4. Click the finished run, scroll to Artifacts, download "capesMC-jar", unzip it.
   Inside is capesmc-1.0.0.jar - that is the mod.
5. If the run is red, open it, copy the error text, and send it to Claude to fix.

# Using it
- Put capesmc-1.0.0.jar and Fabric API (26.3) in .minecraft/mods, launch the Fabric profile, press K.

# Publishing on Modrinth
Create project -> upload capesmc-1.0.0.jar as a version with:
- Loader: Fabric        - Game version: 26.3
- Dependency: Fabric API (required)
- Environment: client required, server optional
- License: MIT          - Icon: assets/capesmc/icon.png (or capesMC-logo.png)
