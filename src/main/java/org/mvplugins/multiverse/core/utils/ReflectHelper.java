package org.mvplugins.multiverse.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReflectHelper {

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static Try<Class<?>> tryGetClass(@NotNull String classPath) {
        return Try.of(() -> Class.forName(classPath));
    }

    public static boolean hasClass(String classPath) {
        return tryGetClass(classPath).isSuccess();
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static <C> Try<Method> tryGetMethod(@NotNull Class<C> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        return Try.of(() -> clazz.getMethod(methodName, parameterTypes))
                .orElse(Try.of(() -> {
                    Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    method.setAccessible(true);
                    return method;
                }));
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean hasMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        return tryGetMethod(clazz, methodName, parameterTypes).isSuccess();
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    @SuppressWarnings("unchecked")
    public static <C, R> Try<R> tryInvokeMethod(@NotNull C classInstance, @NotNull Method method, Object...parameters) {
        return Try.of(() -> (R) method.invoke(classInstance, parameters));
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    @SuppressWarnings("unchecked")
    public static <R> Try<R> tryInvokeStaticMethod(@NotNull Method method, Object...parameters) {
        return Try.of(() -> (R) method.invoke(null, parameters));
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static <C> Try<Field> tryGetField(@NotNull Class<C> clazz, @NotNull String fieldName) {
        return Try.of(() -> clazz.getField(fieldName))
                .orElse(Try.of(() -> {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                }));
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean hasField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        return tryGetField(clazz, fieldName).isSuccess();
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static <C, V> Try<V> tryGetFieldValue(@NotNull C classInstance, @NotNull Field field, @NotNull Class<V> fieldType) {
        return Try.of(() -> fieldType.cast(field.get(classInstance)));
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static <V> Try<V> tryGetStaticFieldValue(@NotNull Field field, @NotNull Class<V> fieldType) {
        return Try.of(() -> fieldType.cast(field.get(null)));
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static Class<?> getClass(String classPath) {
        try {
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static <C> Method getMethod(Class<C> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static <C> Method getMethod(C classInstance, String methodName, Class<?>... parameterTypes) {
        return getMethod(classInstance.getClass(), methodName, parameterTypes);
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    @SuppressWarnings("unchecked")
    public static <C, R> R invokeMethod(C classInstance, Method method, Object...parameters) {
        try {
            return (R) method.invoke(classInstance, parameters);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static <C> Field getField(Class<C> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static <C> Field getField(C classInstance, String fieldName) {
        return getField(classInstance.getClass(), fieldName);
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static <C, V> V getFieldValue(C classInstance, @Nullable Field field, @NotNull Class<V> fieldType) {
        try {
            if (field == null) {
                return null;
            }
            Object value = field.get(classInstance);
            return fieldType.isInstance(value) ? fieldType.cast(value) : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @Nullable
    public static <C, V> V getFieldValue(C classInstance, @Nullable String fieldName, @NotNull Class<V> fieldType) {
        return getFieldValue(classInstance, getField(classInstance, fieldName), fieldType);
    }
}
