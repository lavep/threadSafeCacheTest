package com.gft.cache.LRUCache;

import com.gft.cache.Cache;
import com.gft.cache.lru.LRUCache;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by e-papz on 7/26/2016.
 */
public class LRUCacheTest {

    @Test
    public void simpleAddTest() {
        Cache<Integer, String> cache = new LRUCache(4);
        cache.put(1, "test1");
        cache.put(2, "test2");
        Assert.assertEquals(2, cache.size());
        Assert.assertEquals("test1", cache.get(1));
        Assert.assertEquals("test2", cache.get(2));
        cache.put(3, "test3");
        cache.put(4, "test4");
        Assert.assertEquals(4, cache.size());
        Assert.assertEquals("test1", cache.get(1));
        Assert.assertEquals("test2", cache.get(2));
        Assert.assertEquals("test4", cache.get(4));
        cache.put(5, "test5");

        Assert.assertNull(cache.get(3));
        Assert.assertEquals("test5", cache.get(5));
        Assert.assertEquals("test5", cache.get(5));
        Assert.assertEquals("test5", cache.get(5));
        Assert.assertEquals("test5", cache.get(5));

        cache.put(6, "test6");

        Assert.assertEquals("test4", cache.get(4));
        Assert.assertEquals("test6", cache.get(6));
        Assert.assertNull(cache.get(1));
        Assert.assertEquals("test2", cache.get(2));
        Assert.assertEquals("test5", cache.get(5));
    }

    @Test
    public void multiThreaded() throws InterruptedException {
        final Cache<Integer, String> cache = new LRUCache(4);
        ExecutorService executor = Executors.newFixedThreadPool(300);

        for (int i = 0; i < 300; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        Random rand = new Random();
                        for (int i = 0; i < 10000; i++) {
                            int randValue = rand.nextInt(10);
                            cache.put(randValue, "test" + randValue);
                            try {
                                TimeUnit.MICROSECONDS.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String fromCache = cache.get(randValue);
                         //   System.out.println(i + " " + fromCache);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
              //      System.out.println("End");
                }
            });
        }
        executor.shutdown();
       executor.awaitTermination(100,TimeUnit.SECONDS);


    }

    @Test
    public void multiThreaded2() throws InterruptedException {
        final Cache<Integer, String> cache = new LRUCache(4);
        ExecutorService executor = Executors.newFixedThreadPool(1300);
        cache.put(0,"test0");
        cache.put(1,"test1");
        cache.put(2,"test2");
        cache.put(3,"test3");


        for (int i = 0; i < 1300; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    try {

                        for (int i = 0; i < 10000; i++) {

                            try {
                                TimeUnit.MICROSECONDS.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String fromCache = cache.get(i%4);
                            Assert.assertEquals("test"+i%4,fromCache);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(100,TimeUnit.SECONDS);


    }


}
