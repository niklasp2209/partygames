package com.github.deroq1337.partygames.api.user;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PartyGamesUserRegistry<U extends PartyGamesUser> {

    @NotNull U addUser(@NotNull UUID uuid, boolean alive);

    void removeUser(@NotNull UUID uuid);

    Optional<U> getUser(@NotNull UUID uuid);

    Optional<U> getAliveUser(@NotNull UUID uuid);

    @NotNull Collection<U> getAliveUsers();

    @NotNull Collection<U> getUsers();
}
