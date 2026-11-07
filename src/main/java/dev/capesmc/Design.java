package dev.capesmc;

/**
 * A pixel design on a 16-dye palette (+ transparent).
 * Cape = 10x16 (the visible outer face of the vanilla cape texture), Banner = 20x40.
 * Code format: CS1-<w>x<h>-<run-length data>, e.g. CS1-10x16-o160
 * (a..p = the 16 dye colours, q = transparent, number = repeat count).
 */
public final class Design {
    public static final int CAPE_W = 10, CAPE_H = 16, BANNER_W = 20, BANNER_H = 40;
    public static final int TRANSPARENT = 16;
    public static final String[] NAMES = {"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime",
            "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black", "Clear"};
    public static final int[] PALETTE = {0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F, 0xF38BAA,
            0x474F52, 0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA, 0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21};

    public final int w, h;
    public final byte[] px;

    public Design(int w, int h) { this.w = w; this.h = h; this.px = new byte[w * h]; }

    public static Design blank(int w, int h, int fill) {
        Design d = new Design(w, h);
        java.util.Arrays.fill(d.px, (byte) fill);
        return d;
    }

    public boolean isCape() { return w == CAPE_W && h == CAPE_H; }
    public boolean isBanner() { return w == BANNER_W && h == BANNER_H; }

    public int argb(int c) { return c == TRANSPARENT ? 0 : 0xFF000000 | PALETTE[c]; }

    public void fill(int x, int y, int color) {
        if (x < 0 || y < 0 || x >= w || y >= h) return;
        int target = px[y * w + x];
        if (target == color) return;
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{x, y});
        while (!q.isEmpty()) {
            int[] p = q.poll();
            if (p[0] < 0 || p[1] < 0 || p[0] >= w || p[1] >= h || px[p[1] * w + p[0]] != target) continue;
            px[p[1] * w + p[0]] = (byte) color;
            q.add(new int[]{p[0] + 1, p[1]}); q.add(new int[]{p[0] - 1, p[1]});
            q.add(new int[]{p[0], p[1] + 1}); q.add(new int[]{p[0], p[1] - 1});
        }
    }

    public String encode() {
        StringBuilder sb = new StringBuilder("CS1-").append(w).append('x').append(h).append('-');
        int i = 0;
        while (i < px.length) {
            int c = px[i], run = 1;
            while (i + run < px.length && px[i + run] == c) run++;
            sb.append((char) ('a' + c));
            if (run > 1) sb.append(run);
            i += run;
        }
        return sb.toString();
    }

    /** Returns null if the code is invalid. Safe to call on untrusted input. */
    public static Design decode(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.length() > 4096 || !s.startsWith("CS1-")) return null;
            String[] head = s.substring(4).split("-", 2);
            String[] dim = head[0].split("x");
            int w = Integer.parseInt(dim[0]), h = Integer.parseInt(dim[1]);
            if (!((w == CAPE_W && h == CAPE_H) || (w == BANNER_W && h == BANNER_H))) return null;
            Design d = new Design(w, h);
            String body = head[1];
            int pos = 0, i = 0;
            while (i < body.length()) {
                int c = body.charAt(i++) - 'a';
                if (c < 0 || c > TRANSPARENT) return null;
                int run = 0; boolean has = false;
                while (i < body.length() && Character.isDigit(body.charAt(i))) {
                    run = run * 10 + (body.charAt(i++) - '0');
                    has = true;
                    if (run > 10000) return null;
                }
                if (!has) run = 1;
                if (pos + run > d.px.length) return null;
                for (int k = 0; k < run; k++) d.px[pos++] = (byte) c;
            }
            return pos == d.px.length ? d : null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
