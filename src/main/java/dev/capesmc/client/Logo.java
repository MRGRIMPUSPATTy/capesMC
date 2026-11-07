package dev.capesmc.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The capesMC logo, drawn as pixel art with plain fills (no texture loading, so it can't go missing). */
public final class Logo {
    public static final int SIZE = 28;
    private static final int[] COLORS = {0xFF4A3450, 0xFFFFB9D2, 0xFFF7A8C8, 0xFFA8EFF3, 0xFF7FD8E0, 0xFFFFFFFF}; // A..F
    private static final String[] ROWS = {
        "............................",
        "............................",
        "..C.........................",
        "..C...........A.............",
        "CCCCC....AAAAACAAAAA........",
        "..CC.....AAAAAFAAAAA........",
        "..C......AACCAACEEAA........",
        ".........ACCBBCBDDAA.....EE.",
        ".........ACCBBCBBDEA.....EE.",
        "........AABCBBCBBEEA........",
        "........AABCBBCBBEDAA.......",
        "........ACBBBBBBBEDEA.......",
        ".......AABCBBCBBBEDEA.......",
        ".......AABCBBCBBBEDDAA......",
        ".......ACBBBBCBBBEDDEA......",
        "......AABCBBBCBBBEDDEA......",
        "......ACBCBBBBBBBEDDDAA.....",
        ".....AACBBBBBBBBBEDDDEA.....",
        ".....AABCBBBCBBBBEBDDDAA....",
        ".....ACBCBBBCBBBBDBDDDEA....",
        "....AABBCBBBCBBBBDBDDDDAA...",
        "....ACBBCBBCCAAACBDDDDDAA...",
        "...AABBBCCCAAAAAAAEBDDDEA...",
        "...AAACCAAAA.....AAAEEAAA...",
        ".....AAAAA.........AAAAA....",
        "............................",
        "............................",
        "............................"
    };

    private Logo() { }

    /** Draws the logo with its top-left corner at (x, y); scale 1 = 28x28 GUI pixels. */
    public static void draw(GuiGraphicsExtractor g, int x, int y, int scale) {
        for (int j = 0; j < ROWS.length; j++) {
            String r = ROWS[j];
            int i = 0;
            while (i < r.length()) {
                char c = r.charAt(i);
                if (c == '.') { i++; continue; }
                int run = 1;
                while (i + run < r.length() && r.charAt(i + run) == c) run++;
                g.fill(x + i * scale, y + j * scale, x + (i + run) * scale, y + (j + 1) * scale, COLORS[c - 'A']);
                i += run;
            }
        }
    }
}
