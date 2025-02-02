package com.github.deroq1337.partygames.core.data.game.states;

import com.github.deroq1337.partygames.api.game.PartyGame;
import com.github.deroq1337.partygames.api.scoreboard.GameScoreboard;
import com.github.deroq1337.partygames.api.state.GameState;
import com.github.deroq1337.partygames.api.state.PartyGamesState;
import com.github.deroq1337.partygames.core.data.game.PartyGamesGame;
import com.github.deroq1337.partygames.core.data.game.models.CurrentGame;
import com.github.deroq1337.partygames.core.data.game.provider.PartyGameManifest;
import com.github.deroq1337.partygames.core.data.game.scoreboard.PartyGamesInGameScoreboard;
import com.github.deroq1337.partygames.core.data.game.tasks.PartyGameChooseTask;
import com.github.deroq1337.partygames.core.data.game.tasks.PartyGameLoadTask;
import com.github.deroq1337.partygames.core.data.game.user.DefaultPartyGamesUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class PartyGamesInGameState implements PartyGamesState {

    private final @NotNull PartyGamesGame<DefaultPartyGamesUser> game;
    private final @NotNull GameScoreboard<DefaultPartyGamesUser> scoreboard;
    private final @NotNull Set<PartyGameManifest> playableGames;

    private Optional<CurrentGame> currentGame = Optional.empty();

    public PartyGamesInGameState(@NotNull PartyGamesGame<DefaultPartyGamesUser> game) {
        this.game = game;
        this.scoreboard = new PartyGamesInGameScoreboard(game);
        this.playableGames = game.getGameProvider().getPartyGameManifests();
    }

    @Override
    public void enter() {
        game.setCurrentState(this);

        game.getBoard().ifPresent(board -> game.getUserRegistry().getAliveUsers().forEach(user -> {
            user.getBukkitPlayer().ifPresent(player ->
                    Optional.ofNullable(board.getStartLocation()).ifPresent(startLocation -> player.teleport(startLocation.toBukkitLocation())));
            user.initDice();
            scoreboard.setScoreboard(user);

            new PartyGameChooseTask(game, this).start();
        }));
    }

    @Override
    public void leave() {
        currentGame.ifPresent(endedGame -> game.getGameProvider().unloadGame(endedGame.getManifest()));
        this.currentGame = Optional.empty();
        scoreboard.cancelScoreboardUpdate();
    }

    @Override
    public void onPlayerJoin(@NotNull UUID uuid) {
        DefaultPartyGamesUser user = game.getUserRegistry().addUser(uuid, false);
        scoreboard.setScoreboard(user);
    }

    @Override
    public void onPlayerQuit(@NotNull UUID uuid) {

    }

    public void playGame(@NotNull PartyGameManifest manifest) {
        PartyGame<?, ?, ?> partyGame = game.getGameProvider().loadGame(manifest)
                .orElseThrow(() -> new RuntimeException("Could not load game '" + manifest.getName() + "'"));
        announceGame(manifest);
        playableGames.remove(manifest);

        new PartyGameLoadTask(game, this, new CurrentGame(partyGame, manifest)).start();
    }

    public void onGameEnd() {
        currentGame.ifPresent(endedGame -> game.getGameProvider().unloadGame(endedGame.getManifest()));
        this.currentGame = Optional.empty();

        game.getBoard().ifPresent(board -> game.getUserRegistry().getUsers().forEach(user -> {
            user.getBukkitPlayer().ifPresent(player -> player.teleport(user.getLastLocation()));

            if (user.isAlive()) {
                user.initDice();
            }
        }));
    }

    private void announceGame(@NotNull PartyGameManifest manifest) {
        game.getUserRegistry().getUsers().forEach(user -> {
            user.getBukkitPlayer().ifPresent(player -> player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f));
            user.sendTitle("game_announcement_title", manifest.getName());
            user.sendMessage("game_announcement_name", manifest.getName());
            user.sendMessage("game_announcement_explanation", manifest.getDescription());
        });
    }

    @Override
    public Optional<GameState> getNextState() {
        return Optional.empty();
    }
}
