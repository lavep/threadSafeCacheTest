package com.gft.cache;


import com.gft.cache.lfu.LFUCache;
import com.gft.cache.lru.LRUCache;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
public class JMHLFUCacheTest {


    private final static int cacheSize = 100000;

    private final static int radnUpTo = 500000;

    private final float evictFactor = 0.75f;

    private Cache<Integer, String> lfuCache = new LFUCache(cacheSize, evictFactor);


    private Cache<Integer, String> lruCache = new LRUCache(cacheSize, evictFactor);

    @Test
    public void
    launchBenchmarkRead() throws Exception {

        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName() + ".*read*")
                // Set the following options as needed
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .threads(50)
                .forks(2)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }

    @Test
    public void
    launchBenchmarkRand() throws Exception {

        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName() + ".*Rand*")
                // Set the following options as needed
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .threads(50)
                .forks(2)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }



    @Benchmark
    public void putToCacheLFU() {
        int rand = getRand();
        lfuCache.put(rand, getValue(rand));
    }

    @Benchmark
    public String getFromCacheLFU() {
        int rand = getRand();
        return lfuCache.get(rand);
    }

    @Benchmark
    public String readWriteLFU() {
        int rand = getRand();
        lfuCache.put(rand, getValue(rand));
        int rand2 = getRand();
        return lfuCache.get(rand2);
    }

    @Benchmark
    public String read2WriteLFU() {
        int rand2 = getRand();
        lfuCache.get(rand2);
        int rand = getRand();
        lfuCache.put(rand, getValue(rand));

        int rand3 = getRand();
        return lfuCache.get(rand3);
    }

    @Benchmark
    public String read4WriteLFU() {
        for (int i = 0; i <= 2; i++) {
            int rand2 = getRand();
            lfuCache.get(rand2);
        }
        int rand = getRand();
        lfuCache.put(rand, getValue(rand));

        int rand3 = getRand();
        return lfuCache.get(rand3);
    }


    @Benchmark
    public String read10WriteLFU() {
        for (int i = 0; i <= 9; i++) {
            int rand2 = getRand();
            lfuCache.get(rand2);
        }
        int rand = getRand();
        lfuCache.put(rand, getValue(rand));

        int rand3 = getRand();
        return lfuCache.get(rand3);
    }


    @Benchmark
    public String readWriteLRU() {
        int rand = getRand();
        lruCache.put(rand, getValue(rand));
        int rand2 = getRand();
        return lruCache.get(rand2);
    }

    @Benchmark
    public String read2WriteLRU() {
        int rand = getRand();
        lruCache.get(rand);
        rand = getRand();
        lruCache.put(rand, getValue(rand));
        int rand2 = getRand();
        return lruCache.get(rand2);
    }

    @Benchmark
    public String read4WriteLRU() {
        for (int i = 0; i <= 2; i++) {
            int rand = getRand();
            lruCache.get(rand);
        }
        int rand = getRand();
        lruCache.put(rand, getValue(rand));
        int rand2 = getRand();
        return lruCache.get(rand2);
    }

    @Benchmark
    public String read10WriteLRU() {
        for (int i = 0; i <= 9; i++) {
            int rand = getRand();
            lruCache.get(rand);
        }
        int rand = getRand();
        lruCache.put(rand, getValue(rand));
        int rand2 = getRand();
        return lruCache.get(rand2);
    }



    @Benchmark
    public void putToCacheLRU() {
        int rand = getRand();
        lruCache.put(rand, getValue(rand));
    }

    @Benchmark
    public String getFromCacheLRU() {
        int rand = getRand();
        return lruCache.get(rand);
    }

    private Integer getRand() {
//        Random random=new Random();
//        return random.nextInt(1000);
        return ThreadLocalRandom.current().nextInt(0, 1000);
    }

    @Benchmark
    public Integer getRandIntTest() {
        Random random = new Random();
        return random.nextInt(radnUpTo);
    }

    @Benchmark
    public Integer getRandThreadLocalIntTest() {
        return ThreadLocalRandom.current().nextInt(0, 1000);
    }


    private String getValue(int key) {
        return "value" + key;

    }

}
