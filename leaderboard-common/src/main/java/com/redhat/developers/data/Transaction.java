package com.redhat.developers.data;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Transaction
 */
@RegisterForReflection
public class Transaction {

  public int points;
  public boolean correct;
}
