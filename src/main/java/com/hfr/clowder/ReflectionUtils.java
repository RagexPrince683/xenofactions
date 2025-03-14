package com.hfr.clowder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {

    /**
     * Gets a Class by name.
     *
     * @param className The fully qualified name of the class.
     * @return The Class object, or null if not found.
     */
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null; // Class not found, likely missing the mod
        }
    }

    /**
     * Gets the value of a field via reflection.
     *
     * @param instance  The object instance containing the field.
     * @param fieldName The name of the field.
     * @return The field value, or null if an error occurs.
     */
    public static Object getFieldValue(Object instance, String fieldName) {
        if (instance == null) return null;
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Sets the value of a field via reflection.
     *
     * @param instance  The object instance containing the field.
     * @param fieldName The name of the field.
     * @param value     The value to set.
     */
    public static void setFieldValue(Object instance, String fieldName, Object value) {
        if (instance == null) return;
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException e) {
            // Handle failure silently
        } catch (IllegalAccessException e) {
            // Handle failure silently
        }
    }

    /**
     * Invokes a method via reflection.
     *
     * @param instance   The object instance containing the method.
     * @param methodName The name of the method.
     * @param paramTypes The parameter types of the method.
     * @param args       The arguments to pass to the method.
     * @return The result of the method call, or null if an error occurs.
     */
    public static Object invokeMethod(Object instance, String methodName, Class<?>[] paramTypes, Object... args) {
        if (instance == null) return null;
        try {
            Method method = instance.getClass().getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(instance, args);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Gets a static field value from a class.
     *
     * @param clazz     The class containing the static field.
     * @param fieldName The name of the static field.
     * @return The field value, or null if an error occurs.
     */
    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) {
        if (clazz == null) return null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Invokes a static method via reflection.
     *
     * @param clazz      The class containing the static method.
     * @param methodName The name of the method.
     * @param paramTypes The parameter types of the method.
     * @param args       The arguments to pass to the method.
     * @return The result of the method call, or null if an error occurs.
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args) {
        if (clazz == null) return null;
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }
}


