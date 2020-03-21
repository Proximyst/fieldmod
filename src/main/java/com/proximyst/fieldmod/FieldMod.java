package com.proximyst.fieldmod;

import java.lang.reflect.Field;

/**
 * Utility for modifying the `modifiers` field of `Field` on different Java versions.
 * <p>
 * This abstracts away the necessary code to support versions above Java 12 with `modifiers` modifications at runtime.
 */
public class FieldMod {
  private FieldMod() {
  }

  private static FieldModImplementation implementation = null;

  /**
   * Gets the current Java version's {@link FieldMod} instance.
   * <p>
   * The operation is costly, and thus also only done once. Any further calls returns a cached result.
   *
   * @return The current Java version's {@link FieldMod} instance.
   */
  private static FieldModImplementation getCurrent() {
    if (implementation != null) {
      return implementation;
    }

    try {
      // This class only exists in Java 9 and above:
      Class.forName("java.lang.invoke.VarHandle");
      // As we've continued on, it exists.

      implementation = new FieldModImplementationJava9();
    } catch (ClassNotFoundException ignored) {
      // The current is Java 8 or below.
      // As the class cannot be loaded on Java 7 and below, it is already asserted to be Java 8.

      implementation = new FieldModImplementationJava8();
    }

    return implementation;
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
  public static void setup()
      throws IllegalAccessException, NoSuchFieldException {
    getCurrent().setup();
  }

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
  public static void setModifiers(Field field, int modifiers)
      throws IllegalAccessException, NoSuchFieldException {
    getCurrent().setModifiers(field, modifiers);
  }

  /**
   * Sets the modifiers of a field using the current Java version's preferred approach.
   * <p>
   * This is merely a no exceptions thrown version of {@link #setModifiers}. This does not print the stack trace upon
   * catching exceptions, but rather returns a boolean of whether it was successful.
   *
   * @param field     The field to modify the `modifiers` field of.
   * @param modifiers The new `modifiers` field. This must be calculated by the caller and is in no way verified.
   * @return Whether the call was successful.
   */
  public static boolean setModifiersNoThrow(Field field, int modifiers) {
    try {
      setModifiers(field, modifiers);
      return field.getModifiers() == modifiers;
    } catch (IllegalAccessException | NoSuchFieldException ignored) {
      return false;
    }
  }

  /**
   * Sets the modifiers of a field using the current Java version's preferred approach.
   * <p>
   * This is merely a sneaky throws version of {@link #setModifiers}.
   *
   * @param field     The field to modify the `modifiers` field of.
   * @param modifiers The new `modifiers` field. This must be calculated by the caller and is in no way verified.
   * @throws RuntimeException Exception wrapped with either {@link IllegalAccessException} or {@link
   *                          NoSuchFieldException}.
   */
  public static void setModifiersSneaky(Field field, int modifiers) {
    try {
      setModifiers(field, modifiers);
    } catch (IllegalAccessException | NoSuchFieldException exception) {
      throw new RuntimeException(exception);
    }
  }

  private interface FieldModImplementation {
    /**
     * Sets up the required fields for the Java version this variant represents.
     * <p>
     * This only performs setup upon the first call; all subsequent calls are no-ops.
     *
     * @throws IllegalAccessException Thrown if the `modifiers` field cannot be accessed on the current JVM
     *                                implementation.
     * @throws NoSuchFieldException   Thrown if the `modifiers` field does not exist on the current JVM implementation.
     */
    void setup()
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
    void setModifiers(Field field, int modifiers)
        throws IllegalAccessException, NoSuchFieldException;
  }

  private static class FieldModImplementationJava8 implements FieldModImplementation {
    @Override
    public void setup() throws NoSuchFieldException {
      Java8Implementation.INSTANCE.setup();
    }

    @Override
    public void setModifiers(Field field, int modifiers) throws IllegalAccessException, NoSuchFieldException {
      Java8Implementation.INSTANCE.setModifiers(field, modifiers);
    }
  }

  private static class FieldModImplementationJava9 implements FieldModImplementation {
    @Override
    public void setup() throws IllegalAccessException, NoSuchFieldException {
      Java9Implementation.INSTANCE.setup();
    }

    @Override
    public void setModifiers(Field field, int modifiers) throws IllegalAccessException, NoSuchFieldException {
      // This is not strictly necessary on Java 9, but it is on Java 12+.
      // The implementation requires Java 9 features.

      Java9Implementation.INSTANCE.setModifiers(field, modifiers);
    }
  }
}
