package com.gft.cache.LFUCache;

import com.gft.cache.Cache;
import com.gft.cache.lfu.LFUCache;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by e-papz on 7/26/2016.
 */
public class LFUCacheTest {

    @Test
    public void simpleAddTest() {
        Cache<Integer, String> cache = new LFUCache(4, 0.5);
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
        Assert.assertEquals(3, cache.size());
        Assert.assertNull(cache.get(3));
        Assert.assertEquals("test5", cache.get(5));
        Assert.assertEquals("test5", cache.get(5));
        Assert.assertEquals("test5", cache.get(5));
        Assert.assertEquals("test5", cache.get(5));

        cache.put(6, "test6");
        Assert.assertEquals(4, cache.size());
        Assert.assertNull(cache.get(4));
        Assert.assertEquals("test6", cache.get(6));
        Assert.assertEquals("test1", cache.get(1));
        Assert.assertEquals("test2", cache.get(2));
        Assert.assertEquals("test5", cache.get(5));
    }

    @Test
    public void multiThreaded() throws InterruptedException {
        final Cache<Integer, String> cache = new LFUCache(4, 0.5);
        ExecutorService executor = Executors.newFixedThreadPool(300);
        final CountDownLatch latch=new CountDownLatch(300);
        for (int i = 0; i < 300; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        Random rand = new Random();
                        for (int i = 0; i < 100000; i++) {
                            int randValue = rand.nextInt(10);
                            cache.put(randValue, "test" + randValue);
                            try {
                                TimeUnit.MICROSECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String fromCache = cache.get(randValue);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    latch.countDown();
              //      System.out.println("End");
                }
            });
        }
        executor.shutdown();
        latch.await();
        Assert.assertEquals(0,latch.getCount());


    }
}
