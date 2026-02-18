package net.akat.listeners;

import net.akat.service.BukkitPlaytimeService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class RestrictionListener implements Listener {

    private final BukkitPlaytimeService playtimeService;

    public RestrictionListener(BukkitPlaytimeService playtimeService) {
        this.playtimeService = playtimeService;
    }

    @EventHandler
    public void onPlayerUseFlintAndSteel(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (event.getItem() != null && event.getItem().getType() == Material.FLINT_AND_STEEL && !playtimeService.hasRequiredPlaytime(player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player);
        }

        if (clickedBlock != null && event.getItem() != null && event.getItem().getType() == Material.END_CRYSTAL) {
            Material blockType = clickedBlock.getType();
            if ((blockType == Material.BEDROCK || blockType == Material.OBSIDIAN) && !playtimeService.hasRequiredPlaytime(player)) {
                event.setCancelled(true);
                sendRestrictionMessage(player);
            }
        }

        if (event.getItem() != null && event.getItem().getType() == Material.FIRE_CHARGE && !playtimeService.hasRequiredPlaytime(player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player);
        }
    }

    @EventHandler
    public void onPlayerIgniteCreeper(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Creeper && player.getInventory().getItemInMainHand().getType() == Material.FLINT_AND_STEEL
                && !playtimeService.hasRequiredPlaytime(player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player);
        }
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Creeper creeper) {
            LivingEntity target = creeper.getTarget();
            if (target instanceof Player player && !playtimeService.hasRequiredPlaytime(player)) {
                event.blockList().clear();
                sendRestrictionMessage(player);
            }
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION
                && event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 5, 5, 5).stream()
                .anyMatch(e -> e instanceof Creeper creeper
                        && creeper.getTarget() instanceof Player player
                        && !playtimeService.hasRequiredPlaytime(player))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPlaceTNT(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getType() == Material.TNT && !playtimeService.hasRequiredPlaytime(player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player);
        }
    }

    @EventHandler
    public void onPlayerUseLavaBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (event.getBucket() == Material.LAVA_BUCKET && !playtimeService.hasRequiredPlaytime(player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player);
        }
    }

    @EventHandler
    public void onPlayerAttackEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if ((event.getEntity() instanceof Villager
                    || event.getEntity() instanceof Wolf
                    || event.getEntity() instanceof Cat
                    || event.getEntity() instanceof Parrot)
                    && !playtimeService.hasRequiredPlaytime(player)) {
                event.setCancelled(true);
                sendRestrictionMessage(player);
            }
        }
    }

    @EventHandler
    public void onPlayerGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player && event.isGliding() && !playtimeService.hasRequiredPlaytime(player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player);
        }
    }

    private void sendRestrictionMessage(Player player) {
        String formattedTime = playtimeService.formatRemainingFor(player);
        player.sendMessage(ChatColor.of("#f02c4e") + "⚠ "
                + ChatColor.of("#f35382") + "Вы должны провести на сервере "
                + ChatColor.of("#f9a4bd") + formattedTime
                + ChatColor.of("#f35382") + " времени, чтобы пользоваться этим!");
    }
}
