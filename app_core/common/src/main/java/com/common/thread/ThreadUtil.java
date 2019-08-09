package com.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程抽象类，所有线程在这里进行，防止修改时候改动代码过多 复杂
 * Created by pengminggong on 2016/9/23.
 */
public class ThreadUtil {
    private static ThreadUtil instance = null;
    ExecutorService cachedThreadPool =  Executors.newFixedThreadPool(15);

    public static ThreadUtil getInstance() {
        if (instance == null) {
            synchronized (ThreadUtil.class) {
                instance = new ThreadUtil();
            }
        }
        return instance;
    }


  
    public void addLoopTask(Runnable runnable) {
        cachedThreadPool.execute(runnable);
    }

 
    public void addTask(Runnable runnable) {
        cachedThreadPool.execute(runnable);
    }

}
