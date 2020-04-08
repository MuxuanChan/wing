package com.wing.utils;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Haixiang
 * @email haixiang7c6@gmail.com
 * @since 2020-02-29
 */
@Slf4j
public class StreamUtils {
    private StreamUtils() {
    }

    /**
     * 数组转成列表
     */
    public static <T> List<T> toList(T[] arr){
        return Arrays.stream(arr).collect(Collectors.toList());
    }

    /**
     * 集合过滤
     *
     * @param collection
     * @param predicateCondition
     * @param <T>
     * @return
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicateCondition) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().filter(Objects::nonNull).filter(predicateCondition).collect(Collectors.toList());
    }

    /**
     * 集合过滤
     *
     * @param collection
     * @param predicateCondition
     * @param <T>
     * @return
     */
    public static <T> T filterOne(Collection<T> collection, Predicate<T> predicateCondition) {
        if (CollectionUtils.isEmpty(collection)) {
            return null;
        }
        List<T> ts = filter(collection, predicateCondition);
        if (CollectionUtils.isEmpty(ts)) {
            return null;
        }
        return ts.get(0);
    }

    /**
     * 集合对象转换成Map
     *
     * @param collection 集合对象
     * @param keyMapper  key获取方式
     * @param <S>        key
     * @param <T>        value
     * @return
     */
    public static <S, T> Map<S, T> toMap(Collection<T> collection, Function<T, S> keyMapper) {
        return toMap(collection, keyMapper, t -> t);
    }

    /**
     * 集合对象转换成Map
     *
     * @param collection
     * @param keyMapper
     * @param valueMapper
     * @param <S>
     * @param <T>
     * @param <R>
     * @return
     */
    public static <S, T, R> Map<S, R> toMap(Collection<T> collection, Function<T, S> keyMapper, Function<T, R>
            valueMapper) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * 对集合分组
     *
     * @param collection
     * @param groupByKeyMapper 获取分组key的方式
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> Map<S, List<T>> groupBy(Collection<T> collection, Function<T, S> groupByKeyMapper) {
        return groupBy(collection, groupByKeyMapper, v -> v);
    }

    /**
     * 对集合分组
     *
     * @param collection         集合
     * @param groupByKeyMapper   获取分组key的方式
     * @param groupMembersMapper 处理组内成员的操作
     * @param <S>
     * @param <T>
     * @param <R>
     * @return
     */
    public static <S, T, R> Map<S, List<R>> groupBy(Collection<T> collection, Function<T, S> groupByKeyMapper,
                                                    Function<T, R> groupMembersMapper) {
        return collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(groupByKeyMapper,
                Collectors.mapping
                        (groupMembersMapper, Collectors.toList())));
    }


    private static <T> BinaryOperator<T> retainLastMerger() {
        return (u, v) -> u;
    }

    /**
     * 集合对象转换成Map
     *
     * @param collection
     * @param keyMapper
     * @param valueMapper
     * @param <S>
     * @param <T>
     * @param <R>
     * @return
     */
    public static <S, T, R> Map<S, R> toMapRetainingLast(Collection<T> collection,
                                                         Function<T, S> keyMapper,
                                                         Function<T, R> valueMapper) {
        return toMap(collection, keyMapper, valueMapper, retainLastMerger());
    }

    /**
     * 集合对象转换成Map
     *
     * @param collection
     * @param keyMapper
     * @param valueMapper
     * @param <S>
     * @param <T>
     * @param <R>
     * @return
     */
    public static <S, T, R> Map<S, R> toMapRetainFirstValue(Collection<T> collection,
                                                            Function<T, S> keyMapper,
                                                            Function<T, R> valueMapper) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(keyMapper, valueMapper, (r1, r2) -> r1));
    }

    /**
     * 集合对象转换成Map
     *
     * @param collection
     * @param keyMapper
     * @param valueMapper
     * @param mergeFunction
     * @param <S>
     * @param <T>
     * @param <R>
     * @return
     */
    public static <S, T, R> Map<S, R> toMap(Collection<T> collection, Function<T, S> keyMapper, Function<T, R>
            valueMapper, BinaryOperator<R> mergeFunction) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, valueMapper,
                mergeFunction));
    }

    /**
     * 收集关键属性
     *
     * @param collection
     * @param keyPropertyMapper
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> collectKeyProperty(Collection<T> collection, Function<T, S> keyPropertyMapper) {
        return convert(collection, keyPropertyMapper);
    }

    /**
     * 收集关键属性
     *
     * @param collection
     * @param keyPropertyMapper
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> collectDistinctKeyProperty(Collection<T> collection, Function<T, S>
            keyPropertyMapper) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().filter(Objects::nonNull).map(keyPropertyMapper).distinct().collect(Collectors
                .toList());
    }

    /**
     * 去重
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * 转换列表
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> convert(Collection<T> collection, Function<T, S> convertFunction) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().filter(Objects::nonNull).map(convertFunction).collect(Collectors.toList());
    }

    /**
     * 转换列表
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> convertAndFilterNull(Collection<T> collection, Function<T, S> convertFunction) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(convertFunction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 先过滤再转换列表
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> convert(Collection<T> collection, Predicate<? super T> predicate, Function<T, S> convertFunction) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().filter(Objects::nonNull)
                .filter(predicate)
                .map(convertFunction)
                .collect(Collectors.toList());
    }

    /**
     * 转换列表
     *
     * @param collection
     * @param targetClass
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> convertBeanCopy(Collection<T> collection, Class<S> targetClass) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        Function<T, S> convertFunction = item -> BeanCopierUtils.convert(item, targetClass);
        return convert(collection, convertFunction);
    }

    /**
     * 先排序再转换列表
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> convert(Collection<T> collection, Function<T, S> convertFunction, Comparator<? super T> comparator) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .sorted(comparator)
                .map(convertFunction)
                .collect(Collectors.toList());
    }

    /**
     * 分区
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> Map<Boolean, List<T>> partition(Collection<T> collection, Predicate<? super T> predicate) {
        if (CollectionUtils.isEmpty(collection)) {
            return Maps.newHashMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(predicate));
    }

    /**
     * 先分区再转换
     *
     * @param collection
     * @return
     */
    public static <U, T> Map<Boolean, List<U>> partitionAndConvert(Collection<T> collection, Predicate<? super T> predicate,
                                                                   Function<? super T, ? extends U> mapper) {
        if (CollectionUtils.isEmpty(collection)) {
            return Maps.newHashMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(predicate, Collectors.mapping(mapper, Collectors.toList())));
    }

    /**
     * 平铺转换列表
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> flatConvert(Collection<T> collection, Function<T, Collection<S>> convertFunction) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().filter(Objects::nonNull).flatMap(t -> convertFunction.apply(t).stream()).collect
                (Collectors.toList());
    }

    /**
     * 平铺转换列表
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> flatStreamConvert(Collection<T> collection, Function<T, Stream<S>> convertFunction) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().filter(Objects::nonNull).flatMap(convertFunction::apply).collect(Collectors.toList
                ());
    }

    /**
     * 并发转换
     *
     * @param collection
     * @param convertFunction
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<S> parallelConvert(Collection<T> collection,
                                                 Function<T, S> convertFunction,
                                                 Executor executor) {
        List<CompletableFuture<S>> futures =
                convert(collection, entity ->
                        CompletableFuture.supplyAsync(
                                () -> convertFunction.apply(entity),
                                executor
                        )
                );

        CompletableFuture<List<S>> resultFuture = FutureUtils.sequence(futures);

        List<S> result = FutureUtils.getResultWithThrowException(resultFuture, e -> log.warn("并发失败", e));
        if (Objects.isNull(result)) {
            return Collections.emptyList();
        }

        return result;
    }

    /**
     * 统计
     */
    public static <T> Long count(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return 0L;
        }
        return collection.stream().count();
    }
}
