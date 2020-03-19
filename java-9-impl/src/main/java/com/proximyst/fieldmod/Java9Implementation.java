package com.proximyst.fieldmod;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

enum Java9Implementation {
  INSTANCE;

  private static final Object lock = new Object();

  private boolean hasSetup = false;
  private MethodHandles.Lookup privateFieldLookup = null;
  private VarHandle modifiersFieldVarHandle = null;

  public void setup()
      throws NoSuchFieldException, IllegalAccessException {
    if (hasSetup) {
      return;
    }
    hasSetup = true;

    if (privateFieldLookup == null) {
      synchronized (lock) {
        if (privateFieldLookup == null) {
          privateFieldLookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        }
      }
    }

    if (modifiersFieldVarHandle == null) {
      synchronized (lock) {
        if (modifiersFieldVarHandle == null) {
          modifiersFieldVarHandle = privateFieldLookup.findVarHandle(Field.class, "modifiers", int.class);
        }
      }
    }
  }

  public void setModifiers(Field field, int modifiers)
      throws NoSuchFieldException, IllegalAccessException {
    setup();

    modifiersFieldVarHandle.set(field, modifiers);
  }
}
