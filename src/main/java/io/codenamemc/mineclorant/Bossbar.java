package io.codenamemc.mineclorant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Bossbar implements Listener {

    private static Main plugin = null;

    private static final Map<Player, BossBar> bossBars = new HashMap<>();

    public Bossbar(Main plugin) {
        Bossbar.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createBossBar(player, ChatColor.YELLOW + "CodenameMC"); // 초기 보스바 생성
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();
        removeBossBar(quittingPlayer);
    }

    private static void createBossBar(Player player, String text) {
        BossBar bossBar = Bukkit.createBossBar(text, BarColor.GREEN, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBars.put(player, bossBar);

        // 보스바 업데이트를 위한 태스크
        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.setProgress(1.0);
            }
        }.runTaskTimer(plugin, 20, 20); // 1초마다 업데이트
    }

    private void removeBossBar(Player player) {
        BossBar bossBar = bossBars.remove(player);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public static void showBossbar(Player player, String newText) {
        BossBar bossBar = bossBars.get(player);
        if (bossBar != null) {
            bossBar.setTitle(newText);
        } else {
            createBossBar(player, newText);
        }
    }
}
