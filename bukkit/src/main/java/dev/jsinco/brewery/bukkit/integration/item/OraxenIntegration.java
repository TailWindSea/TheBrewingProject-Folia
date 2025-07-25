package dev.jsinco.brewery.bukkit.integration.item;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OraxenIntegration implements ItemIntegration, Listener {

    private static final boolean ENABLED = ClassUtil.exists("io.th0rgal.oraxen.api.OraxenItem");
    private CompletableFuture<Void> initializedFuture;

    @Override
    public Optional<ItemStack> createItem(String id) {
        return Optional.ofNullable(OraxenItems.getItemById(id))
                .map(ItemBuilder::build);
    }

    public @Nullable Component displayName(String oraxenId) {
        return OraxenItems.getOptionalItemById(oraxenId)
                .map(ItemBuilder::getDisplayName)
                .map(Component::text)
                .orElse(null);
    }

    @Override
    public @Nullable String itemId(ItemStack itemStack) {
        return OraxenItems.getIdByItem(itemStack);
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return initializedFuture;
    }

    @Override
    public boolean enabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "oraxen";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, TheBrewingProject.getInstance());
        this.initializedFuture = new CompletableFuture<>();

        //Bukkit.getScheduler().runTask(TheBrewingProject.getInstance(), () -> initializedFuture.completeExceptionally(new TimeoutException()));
        CompletableFuture.delayedExecutor(60, TimeUnit.SECONDS).execute(() -> {
            if (!initializedFuture.isDone()) {
                initializedFuture.completeExceptionally(new TimeoutException("OraxenItemsLoadedEvent wasn't fired in time"));
            }
        });
    }

    @EventHandler
    public void onOraxenItemsLoaded(OraxenItemsLoadedEvent event) {
        initializedFuture.completeAsync(() -> null);
    }

}
