package io.github.tavstaldev.minecorelib.models.command;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data associated with a subcommand in the OpenKits plugin.
 */
public class SubCommandData {
    public String command;
    public String permission;
    public Map<String, Object> arguments;

    /**
     * Constructs a new SubCommandData object with the specified command, permission, and arguments.
     *
     * @param command    the subcommand
     * @param permission the permission required to execute the subcommand
     * @param arguments  the arguments for the subcommand
     */
    public SubCommandData(String command, String permission, Map<String, Object> arguments) {
        this.command = command;
        this.permission = permission;
        this.arguments = arguments;
    }

    /**
     * Checks if the commandSender has the required permission to execute the subcommand.
     *
     * @param commandSender the commandSender to check
     * @return true if the commandSender has the required permission, false otherwise
     */
    public boolean hasPermission(CommandSender commandSender) {
        if (this.permission == null || permission.isBlank())
            return true;
        return commandSender.hasPermission(this.permission);
    }

    /**
     * Sends a localized message to the commandSender with the subcommand details.
     *
     * @param commandSender the commandSender to send the message to
     */
    public void send(PluginBase plugin, CommandSender commandSender, String baseCommand) {
        if (arguments == null)
            return;

        Map<String, Object> args = new HashMap<>(Map.of("command", baseCommand, "subcommand", command));
        var keys = arguments.keySet();

        Player player = null;
        if (commandSender instanceof Player p)
            player = p;

        for (String key : keys) {
            Object param = arguments.get(key);
            if (param == null) {
                args.put(key, "");
                continue;
            }
            if (param instanceof String stringParam && stringParam.isEmpty()) {
                args.put(key, param);
                continue;
            }
            if (player != null)
                args.put(key, plugin.getTranslator().localize(player, param.toString()));
            else
                args.put(key, plugin.getTranslator().localize(param.toString()));
        }

        plugin.sendCommandReply(commandSender, "Commands.Help.Line", args);
    }
}
