package com.proximyst.fieldmod;

import java.lang.reflect.Field;

/**
 * Utility for modifying the `modifiers` field of `Field` on different Java versions.
 * <p>
 * This abstracts away the necessary code to support versions above Java 12 with `modifiers` modifications at runtime.
 */
public enum FieldMod {
  JAVA_8 {
    @Override
    public void setModifiers(Field field, int modifiers)
        throws IllegalAccessException, NoSuchFieldException {
      Java8Implementation.INSTANCE.setModifiers(field, modifiers);
    }

    @Override
    public void setup() throws NoSuchFieldException {
      Java8Implementation.INSTANCE.setup();
    }
  },

  JAVA_9 {
    @Override
    public void setModifiers(Field field, int modifiers)
        throws IllegalAccessException, NoSuchFieldException {
      // This is not strictly necessary on Java 9, but it is on Java 12+.
      // The implementation requires Java 9 features.

      Java9Implementation.INSTANCE.setModifiers(field, modifiers);
    }

    @Override
    public void setup() throws IllegalAccessException, NoSuchFieldException {
      Java9Implementation.INSTANCE.setup();
    }
  };

  /**
   * The current cached {@link FieldMod}.
   * <p>
   * If <code>null</code>, {@link #getCurrent} has not been called yet.
   */
  private static FieldMod cached = null;

  /**
   * Gets the current Java version's {@link FieldMod} instance.
   * <p>
   * The operation is costly, and thus also only done once. Any further calls returns a cached result.
   *
   * @return The current Java version's {@link FieldMod} instance.
   */
  public static FieldMod getCurrent() {
    if (cached != null) {
      return cached;
    }

    try {
      // This class only exists in Java 9 and above:
      Class.forName("java.lang.invoke.VarHandle");
      // As we've continued on, it exists.

      cached = JAVA_9;
    } catch (ClassNotFoundException ignored) {
      // The current is Java 8 or below.
      // As the class cannot be loaded on Java 7 and below, it is already asserted to be Java 8.

      cached = JAVA_8;
    }

    return cached;
  }

  /**
   * Sets up the required fields for the Java version this variant represents.
   * <p>
   * This only performs setup upon the first call; all subsequent calls are no-ops.
   *
   * @throws IllegalAccessException Thrown if the `modifiers` field cannot be accessed on the current JVM
   *                                implementation.
   * @throws NoSuchFieldException   Thrown if the `modifiers` field does not exist on the current JVM implementation.
   */
  public abstract void setup()
      throws IllegalAccessException, NoSuchFieldException;

  /**
   * Sets the modifiers of a field using the current Java version's preferred approach.
   * <p>
   * The exceptions are generally fine to ignore as they're unlikely to be thrown. If wanted to be checked, one can
   * adapt behaviour by first calling {@link #setup}. The exceptions thrown by this method are simply from that very
   * method and may thus be called ahead of this as a single call to {@link #setup} makes all subsequent ones no-ops.
   *
   * @param field     The field to modify the `modifiers` field of.
   * @param modifiers The new `modifiers` field. This must be calculated by the caller and is in no way verified.
   * @throws IllegalAccessException Thrown if the `modifiers` field cannot be accessed on the current JVM
   *                                implementation.
   * @throws NoSuchFieldException   Thrown if the `modifiers` field does not exist on the current JVM implementation.
   */
  public abstract void setModifiers(Field field, int modifiers)
      throws IllegalAccessException, NoSuchFieldException;
}
