package com.redhat.developers.serialization;

import com.redhat.developers.data.GameMessage;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

/**
 * GameMessageDeserializer
 */
public class GameMessageDeserializer extends JsonbDeserializer<GameMessage> {


  public GameMessageDeserializer() {
    super(GameMessage.class);
  }

}
