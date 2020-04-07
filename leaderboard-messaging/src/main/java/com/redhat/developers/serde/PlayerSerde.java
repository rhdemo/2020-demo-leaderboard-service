package com.redhat.developers.serde;

import com.redhat.developers.data.Player;
import io.quarkus.kafka.client.serialization.JsonbSerde;

public class PlayerSerde extends JsonbSerde<Player> {

  public PlayerSerde() {
    super(Player.class);
  }
}
