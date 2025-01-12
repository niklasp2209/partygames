package com.github.deroq1337.partygames.core.data.game.user;

import com.github.deroq1337.partygames.api.user.User;
import com.github.deroq1337.partygames.core.data.game.PartyGamesGame;
import com.github.deroq1337.partygames.core.data.game.dice.Dice;
import com.github.deroq1337.partygames.core.data.game.tasks.FieldJumpTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PartyGamesUser implements User {

    private final @NotNull PartyGamesGame<PartyGamesUser> game;
    private final @NotNull UUID uuid;
    private final boolean alive;

    private int currentField;
    private Optional<Dice> dice;
    private @NotNull Locale locale = Locale.forLanguageTag("de-DE");

    @Override
    public void sendMessage(@NotNull String key, Object... params) {
        getBukkitPlayer().ifPresent(player -> {
            player.sendMessage(getMessage(key, params));
        });
    }

    @Override
    public @NotNull String getMessage(@NotNull String key, Object... params) {
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(game.getLanguageManager().getMessage(locale, key), params));
    }

    public void goToField(int numberOfEyes) {
        this.currentField += numberOfEyes;

        getBukkitPlayer().ifPresent(player -> {
            game.getBoard().flatMap(board -> board.getField(currentField)).ifPresent(field -> {
                Location fieldLocation = field.getLocation().toBukkitLocation();
                new FieldJumpTask(game, player, fieldLocation).runTaskLater(game.getPartyGames(), 2 * 20L);
            });
        });
    }

    public int getFieldRanking() {
        return (int) game.getUserRegistry().getAliveUsers().stream()
                .filter(user -> user.getCurrentField() > currentField)
                .count() + 1;
    }

    public void initDice() {
        this.dice = Optional.of(new Dice(game, this));
    }

    public Optional<Player> getBukkitPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }
}
