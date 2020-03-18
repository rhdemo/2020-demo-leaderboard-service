package com.redhat.developers;

import com.redhat.developers.data.Player;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

/**
 * PlayerDeseraizlier
 */
public class PlayerDeserializer extends JsonbDeserializer<Player> {


  public PlayerDeserializer() {
    super(Player.class);
  }
}
