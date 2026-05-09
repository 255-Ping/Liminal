package com.github.ping.liminal.command;

import com.github.ping.liminal.world.LevelGenerator;
import com.github.ping.liminal.world.LevelGeneratorRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * /liminalworld create &lt;name&gt; &lt;generator-id&gt;  — make a new world that uses a Liminal generator
 * /liminalworld tp     &lt;name&gt;                    — teleport to a loaded world's spawn
 *
 * Helper for testing during development. Production-grade world management belongs in a
 * dedicated module / Multiverse-style integration later.
 */
public final class LiminalWorldCommand implements CommandExecutor, TabCompleter {

    private final LevelGeneratorRegistry registry;

    public LiminalWorldCommand(LevelGeneratorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("/" + label + " <create|tp> ...", NamedTextColor.GRAY));
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> doCreate(sender, label, args);
            case "tp"     -> doTp(sender, label, args);
            default       -> sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
        }
        return true;
    }

    private void doCreate(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("/" + label + " create <world-name> <generator-id>",
                    NamedTextColor.GRAY));
            return;
        }
        String name = args[1];
        String id = args[2];
        Optional<LevelGenerator> gen = registry.get(id);
        if (gen.isEmpty()) {
            sender.sendMessage(Component.text(
                    "Unknown generator: " + id + ". Known: " + String.join(", ", registry.ids()),
                    NamedTextColor.RED));
            return;
        }
        if (Bukkit.getWorld(name) != null) {
            sender.sendMessage(Component.text("World already loaded: " + name, NamedTextColor.RED));
            return;
        }
        WorldCreator wc = new WorldCreator(name)
                .generator(gen.get().chunkGenerator())
                .biomeProvider(gen.get().biomeProvider());
        World world = wc.createWorld();
        if (world == null) {
            sender.sendMessage(Component.text("World creation failed.", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Created world ", NamedTextColor.GREEN)
                .append(Component.text(name, NamedTextColor.YELLOW))
                .append(Component.text(" (" + id + ").", NamedTextColor.GREEN)));
    }

    private void doTp(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("/" + label + " tp <world-name>", NamedTextColor.GRAY));
            return;
        }
        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            sender.sendMessage(Component.text("No such world: " + args[1], NamedTextColor.RED));
            return;
        }
        player.teleportAsync(world.getSpawnLocation());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("create", "tp");
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            List<String> names = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) names.add(w.getName());
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return new ArrayList<>(registry.ids());
        }
        return Collections.emptyList();
    }
}
