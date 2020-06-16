package ru.sbespalov.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Game
{

    static class Player
    {

        String username;

        public String getUsername()
        {
            return username;
        }

    }

    private Long gameId;
    private List<Player> players;

    public Long getGameId()
    {
        return gameId;
    }

    public List<Player> getPlayers()
    {
        return players;
    }

    public static void main(String[] args)
    {
        Set<Long> friendGameIds = null;
        Map<Long, Game> games = null;
        
        friendGameIds.stream()
                     .map(games::get)
                     .flatMap((game) -> game.getPlayers().stream())
                     .map(Player::getUsername)
                     .sorted()
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
