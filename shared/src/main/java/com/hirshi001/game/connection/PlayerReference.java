package com.hirshi001.game.connection;

import com.hirshi001.game.util.UUID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerReference {


    public static final Map<UUID, PlayerReference> idToPlayerReference = new ConcurrentHashMap<>();

    public static void addPlayerReference(PlayerReference playerReference){
        idToPlayerReference.put(playerReference.id, playerReference);
    }

    public static void removePlayerReference(UUID id){
        idToPlayerReference.remove(id);
    }

    public static PlayerReference getPlayerReference(UUID id){
        return idToPlayerReference.get(id);
    }

    public static void clear() {
        idToPlayerReference.clear();
    }



    public UUID id;
    public String name;

    public PlayerReference(UUID id, String name){
        this.id = id;
        this.name = name;
    }

    public PlayerReference(){

    }

    public Player getPlayer(){
        return Player.getPlayerByUUID(id);
    }

}
