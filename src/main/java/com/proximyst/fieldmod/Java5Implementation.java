package com.proximyst.fieldmod;

import java.lang.reflect.Field;

enum Java5Implementation {
  INSTANCE;

  private static final Object lock = new Object();

  private boolean hasSetup = false;
  private Field modifiersField = null;

  public void setup()
      throws NoSuchFieldException {
    if (hasSetup) {
      return;
    }
    hasSetup = true;

    if (modifiersField == null) {
      synchronized (lock) {
        if (modifiersField == null) {
          modifiersField = Field.class.getDeclaredField("modifiers");
          modifiersField.setAccessible(true);
        }
      }
    }
  }

  public void setModifiers(Field field, int modifiers)
      throws NoSuchFieldException, IllegalAccessException {
    setup();

    modifiersField.setInt(field, modifiers);
  }
}
