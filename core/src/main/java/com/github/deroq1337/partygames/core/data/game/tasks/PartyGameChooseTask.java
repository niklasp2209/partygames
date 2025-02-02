package com.github.deroq1337.partygames.core.data.game.tasks;

import com.github.deroq1337.partygames.core.data.game.PartyGamesGame;
import com.github.deroq1337.partygames.core.data.game.provider.PartyGameManifest;
import com.github.deroq1337.partygames.core.data.game.states.PartyGamesInGameState;
import com.github.deroq1337.partygames.core.data.game.user.DefaultPartyGamesUser;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class PartyGameChooseTask extends BukkitRunnable implements Task {

    private final @NotNull PartyGamesGame<DefaultPartyGamesUser> game;
    private final @NotNull PartyGamesInGameState state;
    private final @NotNull Set<PartyGameManifest> playableGames;

    public PartyGameChooseTask(@NotNull PartyGamesGame<DefaultPartyGamesUser> game, @NotNull PartyGamesInGameState state) {
        this.game = game;
        this.state = state;
        this.playableGames = state.getPlayableGames();
    }

    @Override
    public void run() {
        if (playableGames.isEmpty()) {
            System.err.println("There are no more games to play");
            cancel();
            return;
        }

        long notReady = game.getUserRegistry().getAliveUsers().stream()
                .filter(user -> !user.hasDiceRolled() || !user.isLanded())
                .count();

        if (notReady == 0) {
            PartyGameManifest manifest = new ArrayList<>(playableGames).get(ThreadLocalRandom.current().nextInt(playableGames.size()));
            state.playGame(manifest);
            cancel();
        }
    }

    @Override
    public void start() {
        runTaskTimer(game.getPartyGames(), 0L, 5 * 20L);
    }
}
