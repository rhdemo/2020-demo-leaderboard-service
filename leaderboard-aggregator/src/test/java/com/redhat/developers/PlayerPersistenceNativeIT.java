package com.redhat.developers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.NativeImageTest;

/**
 * PlayerPersistenceNativeTest
 */
@NativeImageTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(AggreatorQuarkusTestEnv.class)
@TestInstance(Lifecycle.PER_CLASS)
public class PlayerPersistenceNativeIT extends PlayerPersistenceTest {

}
