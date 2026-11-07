# capesMC (Fabric, Minecraft 26.3)

Paint or paste-a-code capes and banners, plus Fire / Pearlescent / Dark auras.

## Use it
- Press **K** (rebindable) to open the studio.
- Left-click paints, right-click erases, Brush/Fill tools, 16 dye colours.
- The text box always shows your design code (e.g. `CS1-10x16-o160`). Copy it to share, or paste a friend's code and hit "Paste + load".
- **Apply + save** activates your cape + aura (saved in config/capesmc.json).
- **Get banner** gives you a Custom Banner item with your banner design; place it like a normal block. Also in Creative > Colored Blocks (blank until you give it a design).
- If the server also has capesMC, other players running it see your cape + aura. On servers without it, you still see your own.

## Build
Easiest: generate a fresh Fabric 26.3 template at https://fabricmc.net/develop (adds the Gradle wrapper),
then copy this project's `src/` folder, `gradle.properties` values, and `build.gradle` dependencies over it.
Needs JDK 25. Then `./gradlew build` -> `build/libs/capesmc-1.0.0.jar`.

## Compatibility
- Only depends on Fabric API. No renderer replacement, no vanilla method overwrites.
- Cape: a single TAIL injection on the avatar render state; skin/model untouched.
- Auras are client-side particles, no gameplay effect, safe next to other mods and vanilla servers.
- Works with Sodium/Iris-style renderers as it uses vanilla render paths.

## Not compiled/tested yet - likely tweak spots (26.x APIs moved a lot)
1. build.gradle plugin id/version (copy from the official template if it differs).
2. AvatarRendererMixin: method descriptor / PlayerSkin + ClientAsset constructors.
3. BannerRenderer: BlockEntityRenderer / RenderTypes / submitCustomGeometry signatures.
   If it fights you, delete BannerRenderer.java and the one `BlockEntityRenderers.register` line; capes + auras still work.
4. CapesMC: PayloadTypeRegistry method names, FabricBlockEntityTypeBuilder.
5. CustomBannerBlockEntity: applyImplicitComponents parameter type.
