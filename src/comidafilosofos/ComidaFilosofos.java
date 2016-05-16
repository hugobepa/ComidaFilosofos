/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comidafilosofos;

/**
 *resultado:
 * X O O O O (1)
X o X O O (3)
O o X o o (1)
O o X o X (5)
O o O o X (3)
O X O o X (2)
o X O o O (5)
o X O X O (4)
o O o X O (2)
X O o X O (1)
* .....
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ComidaFilosofos {

   private Chopstick[] s;
    private Philosopher[] f;
    private Helper hlp;

    private void init(){
        s = new Chopstick[5];
        f = new Philosopher[5];
        hlp = new Helper();
        for(int i=0; i<5; i++)
            s[i] = new Chopstick();

        for(int i=0; i<5; i++){
            f[i] = new Philosopher(i, s[i], s[(i+4)%5], hlp);
            new Thread(f[i]).start();
        }

    }

    public ComidaFilosofos(){
        init();
    }

    public static void main(String[] args){
        new ComidaFilosofos();
    }
}


/////
class Chopstick{
    private boolean availability;

    public Chopstick(){
        availability = true;
    }

    public boolean getAvailability(){
        return availability;
    }

    public void setAvailability(boolean flag){
        availability = flag;
    }
}

class Helper{
    private Lock mutex = null;
    private Condition[] cond;
    private String[] state;
    private int[] id;

    private void outputState(int id){
        StringBuffer line = new StringBuffer();
        for(int i=0; i<5; i++)
            line.append(state[i] + " ");
        System.out.println(line + "(" + (id+1) + ")");
    }

    public Helper(){
        id = new int[5];
        mutex = new ReentrantLock();
        state = new String[5];
        cond = new Condition[5];
        for(int i=0; i<5; i++){
            id[i] = i;
            state[i] = "O";
            cond[i] = mutex.newCondition();
        }
    }

    public void setState(int id, String s){
        state[id] = s;
    }

    public void grabChopsticks(int id, Chopstick l, Chopstick r){
        mutex.lock();
        try{
            setState(id, "o");
            while(!l.getAvailability() || !r.getAvailability())
                cond[id].await();

            l.setAvailability(false);
            r.setAvailability(false);
            setState(id, "X");
            outputState(id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            mutex.unlock();
        }
    }


    public void releaseChopsticks(int id, Chopstick l, Chopstick r){
        mutex.lock();
        try{
            setState(id, "O");
            l.setAvailability(true);
            r.setAvailability(true);
            cond[(id+1)%5].signalAll();
            cond[(id+4)%5].signalAll();
            outputState(id);
        }finally{
            mutex.unlock();
        }
    }
}


class Philosopher implements Runnable{
    private Helper hlp;
    private Chopstick l, r;
    private int id;
    public Philosopher(int id, Chopstick l, Chopstick r, Helper i){
        this.hlp = i;
        this.l = l;
        this.r = r;
        this.id = id;
    }

    private void eat(){
        try{
            Thread.sleep(2000);
        }catch(InterruptedException e){}
    }

    private void think(){
        try{
            Thread.sleep(2000);
        }catch(InterruptedException e){}
    }

    public void run(){
        while(true){
            hlp.grabChopsticks(id, l, r);
            eat();
            hlp.releaseChopsticks(id, l, r);
            think();
        }
    }
}