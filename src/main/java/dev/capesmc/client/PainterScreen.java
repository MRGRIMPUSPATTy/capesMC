package dev.capesmc.client;

import dev.capesmc.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Paint a cape or banner pixel by pixel, or paste/copy a design code. Light pink + light cyan theme. */
public class PainterScreen extends Screen {
    // ---- theme ----
    private static final int BG_TOP = 0xFFFFE3EE, BG_BOTTOM = 0xFFD6F6FA;
    private static final int PANEL = 0xF2FFFFFF, PINK_EDGE = 0xFFF7A8C8, CYAN_EDGE = 0xFF7FD8E0;
    private static final int PINK = 0xFFFFB9D2, PINK_HOT = 0xFFFF9CC0;
    private static final int CYAN = 0xFFA8EFF3, CYAN_HOT = 0xFF84E3EA;
    private static final int INK = 0xFF4A3450, INK_SOFT = 0xFF8A6F92;
    private static final int CHECK_A = 0xFFFFF4F8, CHECK_B = 0xFFE6FAFC;

    private static final class Btn {
        final Supplier<String> label; final int x, y, w, h; final boolean pink; final Runnable action;
        Btn(Supplier<String> l, int x, int y, int w, int h, boolean pink, Runnable a) {
            label = l; this.x = x; this.y = y; this.w = w; this.h = h; this.pink = pink; action = a;
        }
        boolean hit(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Btn> btns = new ArrayList<>();
    private Design cape, banner;
    private boolean bannerMode, bucket;
    private int color = 14;
    private Aura aura;
    private EditBox codeBox;
    private String status = "";
    private int cx, cy, cell, px, panelBottom;

    public PainterScreen() {
        super(Component.literal("capesMC"));
        Design c = Design.decode(ClientCosmetics.local.cape);
        Design b = Design.decode(ClientCosmetics.local.banner);
        cape = (c != null && c.isCape()) ? c : Design.blank(Design.CAPE_W, Design.CAPE_H, 14);
        banner = (b != null && b.isBanner()) ? b : Design.blank(Design.BANNER_W, Design.BANNER_H, 14);
        aura = Aura.parse(ClientCosmetics.local.aura);
    }

    private Design cur() { return bannerMode ? banner : cape; }

    @Override
    protected void init() {
        btns.clear();
        Design d = cur();
        cell = Math.max(3, Math.min(14, (height - 90) / d.h));
        cx = 28; cy = 46;
        px = cx + d.w * cell + 40;
        int rw = 232;

        btns.add(new Btn(() -> bannerMode ? "Editing: Banner" : "Editing: Cape", px, 46, 112, 20, true,
                () -> { bannerMode = !bannerMode; rebuildWidgets(); }));
        btns.add(new Btn(() -> bucket ? "Tool: Fill" : "Tool: Brush", px + 120, 46, 112, 20, false,
                () -> bucket = !bucket));

        int py = 46 + 20 + 12 + 3 * 22 + 36;     // below the palette
        btns.add(new Btn(() -> "Fill all", px, py, 72, 20, false,
                () -> { java.util.Arrays.fill(cur().px, (byte) color); refreshCode(); }));
        btns.add(new Btn(() -> "Aura: " + aura.label, px + 80, py, 152, 20, true,
                () -> aura = aura.next()));

        codeBox = new EditBox(font, px, py + 34, rw, 20, Component.literal("Design code"));
        codeBox.setMaxLength(4096);
        addRenderableWidget(codeBox);
        refreshCode();

        btns.add(new Btn(() -> "Copy", px, py + 60, 72, 20, false, () -> {
            minecraft.keyboardHandler.setClipboard(codeBox.getValue()); status = "Code copied";
        }));
        btns.add(new Btn(() -> "Paste + load", px + 80, py + 60, 88, 20, false, () -> {
            codeBox.setValue(minecraft.keyboardHandler.getClipboard()); loadCode();
        }));
        btns.add(new Btn(() -> "Load", px + 176, py + 60, 56, 20, false, this::loadCode));

        btns.add(new Btn(() -> "Apply + save", px, py + 92, 112, 22, true, () -> {
            ClientCosmetics.local.cape = cape.encode();
            ClientCosmetics.local.banner = banner.encode();
            ClientCosmetics.local.aura = aura.name();
            ClientCosmetics.save();
            ClientCosmetics.sendLocal();
            status = "Saved! Cape + aura are active.";
        }));
        btns.add(new Btn(() -> "Get banner", px + 120, py + 92, 112, 22, false, () -> {
            if (ClientPlayNetworking.canSend(BannerPayload.TYPE)) {
                ClientPlayNetworking.send(new BannerPayload(banner.encode()));
                status = "Banner added to your inventory";
            } else status = "This server doesn't have capesMC";
        }));
        btns.add(new Btn(() -> "Done", px, py + 120, rw, 20, false, this::onClose));
        panelBottom = py + 150;
    }

    private void refreshCode() { if (codeBox != null) codeBox.setValue(cur().encode()); }

    private void loadCode() {
        Design d = Design.decode(codeBox.getValue());
        if (d == null) { status = "That code isn't valid"; return; }
        if (d.isCape()) { cape = d; bannerMode = false; } else { banner = d; bannerMode = true; }
        status = "Loaded";
        rebuildWidgets();
    }

    private int swX(int i) { return px + (i % 6) * 22; }
    private int swY(int i) { return 46 + 20 + 12 + (i / 6) * 22; }

    private void paintAt(double mx, double my, int button) {
        Design d = cur();
        int x = (int) Math.floor((mx - cx) / cell), y = (int) Math.floor((my - cy) / cell);
        if (x < 0 || y < 0 || x >= d.w || y >= d.h) return;
        int c = button == 1 ? Design.TRANSPARENT : color;
        if (bucket) d.fill(x, y, c); else d.px[y * d.w + x] = (byte) c;
        refreshCode();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        for (Btn b : btns)
            if (b.hit(e.x(), e.y())) { b.action.run(); return true; }
        if (super.mouseClicked(e, doubleClick)) return true;
        for (int i = 0; i < 17; i++)
            if (e.x() >= swX(i) && e.x() < swX(i) + 18 && e.y() >= swY(i) && e.y() < swY(i) + 18) { color = i; return true; }
        paintAt(e.x(), e.y(), e.button());
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (super.mouseDragged(e, dx, dy)) return true;
        if (!bucket) paintAt(e.x(), e.y(), e.button());
        return true;
    }

    // ---------- drawing ----------
    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    private static void rrect(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int c) {
        g.fill(x0 + 1, y0, x1 - 1, y1, c);
        g.fill(x0, y0 + 1, x1, y1 - 1, c);
    }

    private static void card(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int edge) {
        rrect(g, x0 - 2, y0 - 2, x1 + 2, y1 + 2, edge);
        rrect(g, x0, y0, x1, y1, PANEL);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        for (int y = 0; y < height; y += 2)
            g.fill(0, y, width, y + 2, lerp(BG_TOP, BG_BOTTOM, y / (float) height));
        Design d = cur();
        card(g, cx - 10, cy - 12, cx + d.w * cell + 10, cy + d.h * cell + 10, PINK_EDGE);
        card(g, px - 10, 46 - 12, px + 242, panelBottom, CYAN_EDGE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);   // background + the code text box
        Design d = cur();

        Logo.draw(g, cx - 10, 3, 1);
        g.text(font, Component.literal("capesMC"), cx + 24, 8, INK);
        g.text(font, Component.literal("left-click paint  |  right-click erase"), cx + 24, 19, INK_SOFT);

        // canvas
        for (int y = 0; y < d.h; y++)
            for (int x = 0; x < d.w; x++) {
                int c = d.px[y * d.w + x];
                int x0 = cx + x * cell, y0 = cy + y * cell;
                g.fill(x0, y0, x0 + cell, y0 + cell, ((x + y) & 1) == 0 ? CHECK_A : CHECK_B);
                if (c != Design.TRANSPARENT) g.fill(x0, y0, x0 + cell, y0 + cell, d.argb(c));
            }

        // palette
        for (int i = 0; i < 17; i++) {
            int x = swX(i), y = swY(i);
            if (i == color) rrect(g, x - 3, y - 3, x + 21, y + 21, PINK_HOT);
            rrect(g, x - 1, y - 1, x + 19, y + 19, i == color ? INK : 0xFFFFFFFF);
            rrect(g, x, y, x + 18, y + 18, i == Design.TRANSPARENT ? CHECK_B : d.argb(i));
            if (i == Design.TRANSPARENT) g.text(font, Component.literal("x"), x + 6, y + 5, INK);
        }
        g.text(font, Component.literal(Design.NAMES[color]), px + 136, swY(0) + 6, INK);

        // buttons
        for (Btn b : btns) {
            boolean hot = b.hit(mouseX, mouseY);
            int base = b.pink ? (hot ? PINK_HOT : PINK) : (hot ? CYAN_HOT : CYAN);
            rrect(g, b.x - 1, b.y - 1, b.x + b.w + 1, b.y + b.h + 1, b.pink ? PINK_EDGE : CYAN_EDGE);
            rrect(g, b.x, b.y, b.x + b.w, b.y + b.h, base);
            String s = b.label.get();
            int tw = font.width(s);
            g.text(font, Component.literal(s), b.x + (b.w - tw) / 2, b.y + (b.h - 8) / 2, INK);
        }

        if (!status.isEmpty()) g.text(font, Component.literal(status), px, panelBottom + 8, INK);
    }

    @Override public boolean isPauseScreen() { return false; }
}
