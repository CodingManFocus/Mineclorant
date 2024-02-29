package io.codenamemc.mineclorant;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Bossbar(this), this);
        getLogger().info("Mineclorant 활성화 완료.");
        getLogger().info("개발 : 포커스Focus 배급: CodenameMC");
    }

    @Override
    public void onDisable() {
        getLogger().info("Mineclorant 비활성화중...");
    }

    //전역변수 선언

    private boolean gameRunning = false;

    private final List<Player> players = new ArrayList<>();

    int playersRemaining;

    int wbtimer = 10;

    private int repeatTask;

    //전역변수 선언

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("start")) {
            startGame(sender);
            return true;
        }
        if (command.getName().equalsIgnoreCase("false")) { gameRunning = false; }
        return false;
    }

    private void startGame(CommandSender sender) {
        playersRemaining = Bukkit.getOnlinePlayers().size();
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어가 실행 해야 합니다!");
            return;
        }

        if (!gameRunning) {
            gameRunning = true;

            // 게임 시작 로직 구현
            Bukkit.broadcastMessage("게임 시작!");

            players.clear();

            wbtimer = 11;

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear @a");

            // 플레이어들을 랜덤한 위치로 텔레포트
            for (Player player : Bukkit.getOnlinePlayers()) {
                Random random = new Random();
                int newX = random.nextInt(600) - 300 + 224; // -300 ~ 299 + 224 = -76 ~ 523
                int newZ = random.nextInt(600) - 300 - 309; // -300 ~ 299 - 309 = -609 ~ -10
                int newY = player.getWorld().getHighestBlockYAt(newX, newZ);

                // 새로운 위치 설정
                Location newLocation = new Location(player.getWorld(), newX, newY, newZ);
                player.teleport(newLocation);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 2400, 3));
                player.setGameMode(GameMode.SURVIVAL);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wm give " + player.getName() + " AK_47");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wm give " + player.getName() + " AX_50");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wm give " + player.getName() + " 357_Magnum");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wm give " + player.getName() + " STG44");
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 8));
                players.add(player);
            }

            // 월드 보더 설정
            WorldBorder worldBorder = Bukkit.getWorlds().get(0).getWorldBorder();
            worldBorder.setCenter(224, -309);
            worldBorder.setSize(300);

            repeatTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                if (gameRunning) {
                    checkWinner();

                    --wbtimer;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Bossbar.showBossbar(player, "다음 월드보더 축소 까지..." + wbtimer + "초");
                    }

                    if (wbtimer <= 0) {
                        double newSize = worldBorder.getSize() - 20;
                        if (newSize > 5) {
                            worldBorder.setSize(newSize, 3);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                Bossbar.showBossbar(player, "월드 보더 축소중...");
                            }
                            wbtimer = 21;
                        }
                    }
                }
            }, 20L, 20L);// 1초마다 실행
        } else {
            sender.sendMessage("게임은 아직 진행중입니다");
        }
    }

    public void checkWinner() {
        //getLogger().info(String.valueOf(players.size()));
        if (players.size() == 1) {
            Player winner = players.get(0);
            Bukkit.broadcastMessage(ChatColor.YELLOW + winner.getName() + "님이 승리하였습니다!");
            Bukkit.getServer().getScheduler().cancelTask(repeatTask);
            gameRunning = false;
            for (Player player : Bukkit.getOnlinePlayers()) { Bossbar.showBossbar(player,winner.getName() + "님이 승리했습니다"); }
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        //getLogger().info(players.size() + "이벤트 헨들러");
        //getLogger().info(gameRunning + "이벤트 헨들러");
        if (gameRunning){
            Player player = event.getEntity().getPlayer();
            Player killer = event.getEntity().getKiller();

            player.setGameMode(GameMode.SPECTATOR);

            if (killer == null) {
                event.setDeathMessage(player.getName() + "님이 사망했습니다");
            } else {
                event.setDeathMessage(player.getName() + "님이" + killer.getName() + "에 의해 TANG GO DOWN!");
            }

            players.remove(player);
            getLogger().info("플레이어 사망");
            if (players.size() > 1) Bukkit.broadcastMessage(players.size() + "명 남았습니다!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitPlayer = event.getPlayer();
        players.removeIf(player -> player.getName().equals(quitPlayer.getName()));
    }
}