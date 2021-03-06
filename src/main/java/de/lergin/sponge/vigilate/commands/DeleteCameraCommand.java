package de.lergin.sponge.vigilate.commands;

import de.lergin.sponge.vigilate.Camera;
import de.lergin.sponge.vigilate.Vigilate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

public class DeleteCameraCommand implements CommandExecutor {
    private Vigilate plugin;

    public  DeleteCameraCommand(Vigilate plugin){
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Camera cam = args.<Camera>getOne("camera").orElseThrow(() -> new CommandException(Text.of("No Camera")));

        plugin.getCameras().remove(cam.getId());
        plugin.getConfig().save();

        src.sendMessage(plugin.translations.CAMERA_DELTED, cam.templateVariables());

        return CommandResult.success();
    }
}
