package com.sqr.demoboot.zpractice.thread.a_004_interview.interview1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * 面试题
 * 一个容器 有add size方法
 * 一个线程添加add  另一个线程到容器size==5时打印  线程1再继续执行
 * ① volatile 加上关键字后 看起来是正常执行了  但其实是因为t1睡了一秒钟
 * ② wait notify  wait会释放锁 notify不会释放锁 所以就必须让t2先执行进行监听
 * 下面这种写法t2等到t1结束才会执行
 * 因为t1虽然notify了t2 但是没有释放锁 t2获取不到锁 还是不会执行
 * 直至t1结束所释放 t2执行
 * --------------------------------------------------------------------------
 * 修改  t1唤醒其他线程 自己wait  t2执行结束 唤醒t1
 * 缺点:通信过于复杂
 * ③ CountDownLatch 门闩倒数
 * await countDown 通信简单  不涉及同步时使用CountDownLatch
 * 下面程序在countDown后 线程1、2都可运行 无法保证运行的先后顺序
 * --------------------------------------------------------------------------
 * 修改 再添加一个门闩 等t2执行完成后t1才能执
 * ④ LockSupport park unpark
 * 下面程序在unpark(t2)后  线程1、2都可运行 无法保证运行的先后顺序
 * --------------------------------------------------------------------------
 * 修改 在unpark(t2)后 t1 park  t2执行结束后unpark(t1)
 */
public class Code04_LockSupport<T> {
    volatile List<T> list = new ArrayList<>();
    public void add(T t){
        list.add(t);
    }
    public int size(){
        return list.size();
    }
    static Thread t1;
    static Thread t2;
    public static void main(String[] args) {
        Code04_LockSupport<Integer> list_ = new Code04_LockSupport<>();
        t2 = new Thread(()->{
            System.out.println("t2 start");
            //默认t2处于阻塞状态
            LockSupport.park();
            System.out.println("t2 exec");
            System.out.println("t2 end");
            LockSupport.unpark(t1);
        });
        t1 = new Thread(()->{
            for (int i = 0; i < 10 ; i++) {
                list_.add(i);
                System.out.println("t1 "+ i);
                if (list_.size()==5){/*等于5 */
                    LockSupport.unpark(t2);
                    LockSupport.park();
                }
            }
        });
        t2.start();
        t1.start();
    }
}
