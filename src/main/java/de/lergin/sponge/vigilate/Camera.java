package de.lergin.sponge.vigilate;

import com.google.common.collect.ImmutableMap;
import de.lergin.sponge.vigilate.data.ViewerData;
import de.lergin.sponge.vigilate.data.VigilateKeys;
import de.lergin.sponge.vigilate.data.ViewerDataManipulatorBuilder;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@ConfigSerializable
public class Camera {
    private Location<World> loc;
    @Setting(value = "name", comment = "Name of the camera")
    private Text name;
    @Setting(value = "id", comment = "Id of the camera")
    private String id;
    @Setting(value = "permission", comment = "Permission needed to use the camera")
    private String permission;

    private Vigilate plugin;

    public Camera(){
        plugin = Vigilate.getInstance();
    }

    public Camera(Location<World> loc, String id) {
        this.loc = loc;
        this.id = id.toLowerCase();

        Vigilate.getInstance().getCameras().put(id, this);
        plugin = Vigilate.getInstance();
    }

    public Location<World> getLocation() {
        return loc;
    }

    public void setLocation(Location<World> loc) {
        this.loc = loc;
    }

    public Text getName() {
        if(name == null){
            return Text.EMPTY;
        }

        return name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        Vigilate.getInstance().getCameras().remove(this.id);

        this.id = id.toLowerCase();

        Vigilate.getInstance().getCameras().put(this.id, this);
    }

    public String getPermission() {
        if(permission == null){
            return "";
        }

        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void viewCamera(Player player){
        if(!this.canUseCamera(player)){
            return; // shouldn't have the ability to even execute the command
        }

        if (player.supports(VigilateKeys.OLD_GAME_MODE)) {
            player.get(VigilateKeys.CAMERA).orElse("");

            if(Vigilate.getInstance().getCameras().containsKey(id)){
                Vigilate.getInstance().getCameras().get(id).endViewCamera(player);
            }
        }

        player.offer(new ViewerDataManipulatorBuilder().create());

        player.getValue(Keys.GAME_MODE).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_GAME_MODE, value.get()))
        );
        player.getValue(Keys.IS_FLYING).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_IS_FLYING, value.get()))
        );
        player.getValue(Keys.AFFECTS_SPAWNING).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_AFFECTS_SPAWNING, value.get()))
        );
        player.getValue(Keys.VANISH).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_VANISH, value.get()))
        );
        player.getValue(Keys.VANISH_PREVENTS_TARGETING).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_VANISH_PREVENTS_TARGETING, value.get()))
        );
        player.getValue(Keys.VANISH_IGNORES_COLLISION).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_VANISH_IGNORES_COLLISION, value.get()))
        );
        player.getValue(Keys.FLYING_SPEED).ifPresent(
                (value -> player.offer(VigilateKeys.OLD_FLYING_SPEED, value.get()))
        );

        player.offer(VigilateKeys.OLD_LOCATION_WORLD, player.getLocation().getExtent().getName());
        player.offer(VigilateKeys.OLD_LOCATION_X, player.getLocation().getX());
        player.offer(VigilateKeys.OLD_LOCATION_Y, player.getLocation().getY());
        player.offer(VigilateKeys.OLD_LOCATION_Z, player.getLocation().getZ());

        player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
        player.offer(Keys.AFFECTS_SPAWNING, false);
        player.offer(Keys.VANISH, true);
        player.offer(Keys.VANISH_PREVENTS_TARGETING, true);
        player.offer(Keys.VANISH_IGNORES_COLLISION, true);
        player.offer(Keys.FLYING_SPEED, 0.0);
        player.offer(Keys.IS_FLYING, true);

        player.offer(VigilateKeys.CAMERA, this.getId());

        player.setLocation(this.getLocation());

        Title title = Title.builder()
                .fadeIn(20)
                .fadeOut(20)
                .title(Text.EMPTY)
                .subtitle(plugin.translations.CAMERA_VIEW_TITLE.apply(this.templateVariables()).toText())
                .stay(100000)
                .build();
        player.sendTitle(title);

        player.sendMessage(plugin.translations.CAMERA_VIEW, this.templateVariables());
    }

    public void endViewCamera(Player player){
        Optional<String> cameraId = player.get(VigilateKeys.CAMERA);

        if (cameraId.isPresent() && !cameraId.get().equals("")) {
            Camera.resetPlayer(player);
            player.sendMessage(plugin.translations.CAMERA_ENDVIEW, this.templateVariables());
        }
    }

    public Boolean canUseCamera(CommandSource src){
        return this.getPermission().equals("") || src.hasPermission(this.getPermission());
    }

    public ImmutableMap<String, TextElement> templateVariables(){
        return ImmutableMap.<String, TextElement>builder()
                .put("camera.id", Text.of(this.getId()))
                .put("camera.name", this.getName())
                .put("camera.permission", Text.of(this.getPermission()))
                .put("camera.world", Text.of(this.getLocation().getExtent().getName()))
                .put("camera.x", Text.of(this.getLocation().getX()))
                .put("camera.y", Text.of(this.getLocation().getY()))
                .put("camera.z", Text.of(this.getLocation().getY()))
                .build();
    }

    static public void resetPlayer(Player player){
        if (player.supports(VigilateKeys.OLD_GAME_MODE)) {

            player.getValue(VigilateKeys.OLD_GAME_MODE).ifPresent(
                    (value -> player.offer(Keys.GAME_MODE, value.get()))
            );
            player.getValue(VigilateKeys.OLD_IS_FLYING).ifPresent(
                    (value -> player.offer(Keys.IS_FLYING, value.get()))
            );
            player.getValue(VigilateKeys.OLD_AFFECTS_SPAWNING).ifPresent(
                    (value -> player.offer(Keys.AFFECTS_SPAWNING, value.get()))
            );
            player.getValue(VigilateKeys.OLD_VANISH).ifPresent(
                    (value -> player.offer(Keys.VANISH, value.get()))
            );
            player.getValue(VigilateKeys.OLD_VANISH_PREVENTS_TARGETING).ifPresent(
                    (value -> player.offer(Keys.VANISH_PREVENTS_TARGETING, value.get()))
            );
            player.getValue(VigilateKeys.OLD_VANISH_IGNORES_COLLISION).ifPresent(
                    (value -> player.offer(Keys.VANISH_IGNORES_COLLISION, value.get()))
            );
            player.getValue(VigilateKeys.OLD_FLYING_SPEED).ifPresent(
                    (value -> player.offer(Keys.FLYING_SPEED, value.get()))
            );

            World world = Sponge.getServer().getWorld(
                    player.get(VigilateKeys.OLD_LOCATION_WORLD).orElse("world")
            ).orElse(Sponge.getServer().getWorlds().iterator().next());

            Location<World> loc = new Location<>(
                    world,
                    player.get(VigilateKeys.OLD_LOCATION_X).orElse(0.0),
                    player.get(VigilateKeys.OLD_LOCATION_Y).orElse(0.0),
                    player.get(VigilateKeys.OLD_LOCATION_Z).orElse(0.0)
            );

            player.setLocation(loc);

            player.remove(ViewerData.class);
        }

        player.clearTitle();
    }
}
