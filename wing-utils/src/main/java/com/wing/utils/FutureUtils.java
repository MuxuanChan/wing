package com.wing.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Muxuan
 * @email muxuanchan@gmail.com
 * @since 2020-02-29
 */
public class FutureUtils {
    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    /**
     * 获取执行结果，如果有异常则打印日志，消费异常，返回结果为null
     * @param completableFuture
     * @param logRunnable
     * @param <T>
     * @return
     */
    public static <T> T getResultWithDrownException(CompletableFuture<T> completableFuture, Runnable logRunnable){
        try {
            return completableFuture
                    .exceptionally(buildThrowableExceptionFunction(logRunnable))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            logRunnable.run();
            return null;
        }
    }
    /**
     * 获取执行结果，如果有异常则打印日志，消费异常，返回结果为null
     * @param completableFuture
     * @param logConsumer
     * @param <T>
     * @return
     */
    public static <T> T getResultWithDrownException(CompletableFuture<T> completableFuture, Consumer<Exception>
            logConsumer){
        try {
            return completableFuture
                    .exceptionally(buildThrowableExceptionFunction(logConsumer))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            logConsumer.accept(e);
            return null;
        }
    }

    private static <T> Function<Throwable, T> buildThrowableExceptionFunction(Consumer<Exception> logConsumer) {
        return e -> {
            logConsumer.accept((Exception) e);
            return null;
        };
    }

    /**
     * 获取执行结果，如果有异常则打印日志，抛出运行时异常
     * @param completableFuture
     * @param logConsumer
     * @param <T>
     * @return
     */
    public static <T> T getResultWithThrowException(CompletableFuture<T> completableFuture, Consumer<Exception> logConsumer){
        try {
            return completableFuture
                    .exceptionally(buildThrowableExceptionFunction(logConsumer))
                    .get();
        } catch (InterruptedException | ExecutionException | RuntimeException e) {
            logConsumer.accept(e);
            throw new RuntimeException(e);
        }
    }

    private static <T> Function<Throwable, ? extends T> buildThrowableExceptionFunction(Runnable logRunnable) {
        return e -> {
            logRunnable.run();
            return null;
        };
    }
}
