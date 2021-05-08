package ciyuanwutuobang;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {
    private Map<String, Date> timer = new HashMap<>();
    private Map<String,Aplayer> ps = new HashMap<>();
    private Map<String, Boolean> IsOnline = new HashMap<>();
    private Integer ride=25,walk=10;
    FileConfiguration config = getConfig();
    private Date lasttime;
    private class Aplayer{
        BukkitTask time;
        Player player;
        Location lastloc;
        Double speed;
        Date timer;
    }

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage("反变速已开启");
        config.addDefault("行走时提示的最低速度", "10");
        config.addDefault("骑乘时提示的最低速度", "25");
        config.options().copyDefaults(true);
        saveConfig();

        walk= Integer.parseInt(config.getString("行走时提示的最低速度"));
        ride= Integer.parseInt(config.getString("骑乘时提示的最低速度"));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("反变速已关闭");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ("ace".equalsIgnoreCase(cmd.getName())) {
            if (!sender.isOp()) {
                sender.sendMessage("§8[ §c反变速 §8]§7你没有权限这么做");
                return true;
            }

            if (args.length!=1) {
                sender.sendMessage("§7§m---------------------§8[ §c反变速 §8]§7§m---------------------");
                sender.sendMessage("  ");
                sender.sendMessage("§7输入/ace [玩家id] 测量玩家当前瞬时速度");
                sender.sendMessage("§7输入/ace reload 重载配置文件");
                sender.sendMessage("  ");
                sender.sendMessage("§7§m---------------------§8[ §c反变速 §8]§7§m---------------------");
                return true;
            }
            String a0 = args[0];
            if ("reload".equals(a0)) {
                walk= Integer.parseInt(config.getString("行走时提示的最低速度"));
                ride= Integer.parseInt(config.getString("骑乘时提示的最低速度"));
                sender.sendMessage("§8[ §c反变速 §8]§7重新载入完成,行走提示速度: §a" + walk + " §7,骑乘提示速度: §a" + ride);
                return true;
            }

            if (IsOnline.get(a0)==null) {
                sender.sendMessage("§8[ §c反变速 §8]§7该玩家不存在");
                return true;
            }

            if (!IsOnline.get(a0)) {
                sender.sendMessage("§8[ §c反变速 §8]§7该玩家不在线");
                return true;
            }
            DecimalFormat d2   = new DecimalFormat("######0.00");
            sender.sendMessage("§8[ §c反变速 §8]§7 玩家 §a" + ps.get(a0).player.getName() + " §7当前速度 §a" + d2.format(ps.get(a0).speed));
            return true;
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

            IsOnline.put(event.getPlayer().getName(), true);
            Aplayer a = new Aplayer();
            a.player = event.getPlayer();
            a.lastloc = event.getPlayer().getLocation();
            a.speed = 0.00;
            a.time = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!IsOnline.get(event.getPlayer().getName())) {
                        this.cancel();
                    }

                    Calendar cal = Calendar.getInstance();
                    Date date = cal.getTime();

                    if (a.lastloc.getWorld().equals(event.getPlayer().getLocation().getWorld()) && lasttime != null) {
                        a.speed = event.getPlayer().getLocation().distance(a.lastloc);
                    }
                    lasttime = date;

                    if (a.lastloc.getWorld().equals(event.getPlayer().getLocation().getWorld())) {
                        if (a.speed > ride && a.speed < 500 && event.getPlayer().getVehicle() != null && !event.getPlayer().isFlying()) {
                                if (ps.get(event.getPlayer().getName()).timer != null) {
                                    if (date.getTime() - ps.get(event.getPlayer().getName()).timer.getTime() <= 1500) {
                                        DecimalFormat d2 = new DecimalFormat("######0.00");
                                        for (Map.Entry<String,Aplayer> entry:ps.entrySet()) {
                                            if (IsOnline.get(entry.getValue().player.getName())!=null) {
                                                if (IsOnline.get(entry.getValue().player.getName())) {
                                                    if (entry.getValue().player.isOp() ) {
                                                        entry.getValue().player.sendMessage("§8[ §c反变速 §8]§7 玩家乘坐载具 §a" + event.getPlayer().getName() + " §7移速过快,速度 §a" + d2.format(a.speed));
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                                a.timer = date;
                        }
                    }
                    if (a.lastloc.getWorld().equals(event.getPlayer().getLocation().getWorld())) {
                        if (a.speed > walk && a.speed < 500 && event.getPlayer().getVehicle() == null
                                && a.lastloc.getY() - event.getPlayer().getLocation().getY() < 7 && !event.getPlayer().isFlying()) {
                                if (ps.get(event.getPlayer().getName()).timer != null) {
                                    if (date.getTime() - ps.get(event.getPlayer().getName()).timer.getTime() <= 1500) {
                                        DecimalFormat d2 = new DecimalFormat("######0.00");
                                        for (Map.Entry<String,Aplayer> entry:ps.entrySet()) {
                                            if (IsOnline.get(entry.getValue().player.getName())!=null) {
                                                if (IsOnline.get(entry.getValue().player.getName())) {
                                                    if (entry.getValue().player.isOp() ) {
                                                        entry.getValue().player.sendMessage("§8[ §c反变速 §8]§7 玩家步行 §a" + event.getPlayer().getName() + " §7移速过快,速度 §a" + d2.format(a.speed));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                a.timer = date;

                        }
                    }
                    a.lastloc = event.getPlayer().getLocation();
                    ps.put(event.getPlayer().getName(), a);


                }
            }.runTaskTimer(this, 20, 20);
        }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        IsOnline.put(event.getPlayer().getName(),false);
    }

}
