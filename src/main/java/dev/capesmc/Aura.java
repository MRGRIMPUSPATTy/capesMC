package dev.capesmc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/** Purely cosmetic, client-side particle auras. No gameplay effect. */
public enum Aura {
    NONE("None"), FIRE("Fire"), PEARLESCENT("Pearlescent"), DARK("Dark");

    public final String label;
    Aura(String label) { this.label = label; }

    public Aura next() { return values()[(ordinal() + 1) % values().length]; }

    public static Aura parse(String s) {
        try { return valueOf(s); } catch (Exception e) { return NONE; }
    }

    public void tick(ClientLevel lvl, Player p, RandomSource r, long t) {
        switch (this) {
            case FIRE -> {
                for (int i = 0; i < 2; i++)
                    lvl.addParticle(ParticleTypes.FLAME, x(p, r), y(p, r), z(p, r), 0, 0.03, 0);
                if (t % 3 == 0) lvl.addParticle(ParticleTypes.SMALL_FLAME, x(p, r), y(p, r), z(p, r), 0, 0.05, 0);
                if (t % 8 == 0) lvl.addParticle(ParticleTypes.SMOKE, x(p, r), p.getY() + 2.0, z(p, r), 0, 0.04, 0);
            }
            case PEARLESCENT -> {
                for (int i = 0; i < 2; i++) {
                    float hue = ((t * 0.01f) + r.nextFloat() * 0.35f) % 1f;
                    int rgb = Mth.hsvToRgb(hue, 0.35f, 1f);   // soft pastel shimmer that drifts through hues
                    lvl.addParticle(new DustParticleOptions(rgb, 0.8f), x(p, r), y(p, r), z(p, r), 0, 0.01, 0);
                }
                if (t % 5 == 0) lvl.addParticle(ParticleTypes.END_ROD, x(p, r), y(p, r), z(p, r), 0, 0.02, 0);
            }
            case DARK -> {
                for (int i = 0; i < 2; i++)
                    lvl.addParticle(new DustParticleOptions(0x2A0845, 1.2f), x(p, r), y(p, r), z(p, r), 0, 0.01, 0);
                if (t % 3 == 0) lvl.addParticle(ParticleTypes.SMOKE, x(p, r), y(p, r), z(p, r), 0, 0.02, 0);
                if (t % 10 == 0) lvl.addParticle(ParticleTypes.SQUID_INK, x(p, r), y(p, r), z(p, r), 0, 0.01, 0);
            }
            default -> { }
        }
    }

    private static double x(Player p, RandomSource r) { return p.getX() + (r.nextDouble() - 0.5) * 1.0; }
    private static double y(Player p, RandomSource r) { return p.getY() + r.nextDouble() * 1.9; }
    private static double z(Player p, RandomSource r) { return p.getZ() + (r.nextDouble() - 0.5) * 1.0; }
}
