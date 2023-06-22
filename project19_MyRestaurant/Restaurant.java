package project19;
import java.util.concurrent.*;


class Meal {
  private final int orderNum;
  public Meal(int orderNum) { this.orderNum = orderNum; }
  public String toString() { return "Meal " + orderNum; }
}

class WaitPerson implements Runnable {
  private Restaurant restaurant;
  public WaitPerson(Restaurant r) { restaurant = r; }
  protected boolean isClean=true;
  protected Meal meal;
  public void run() {
    try {
      while(!Thread.interrupted()) {
        synchronized(this) {
          while(restaurant.meal == null)
            wait(); 
        }
        meal=restaurant.meal;
      System.out.println("Waitperson got " + restaurant.meal);
        synchronized(restaurant.chef) {
          restaurant.meal = null;
          restaurant.chef.notifyAll();
        }
        System.out.println("WaitPerson delivered "+meal);
        synchronized(restaurant.busBoy) {
        	isClean=false;
        	restaurant.busBoy.notifyAll();
        }
      }
    } catch(InterruptedException e) {
    	 System.out.println("WaitPerson interrupted");
    }
  }
}
class BusBoy implements Runnable{
	  private Restaurant restaurant;
	  public BusBoy(Restaurant r) { restaurant = r; }
      public void run() {
    	  try {
    		      while(!Thread.interrupted()) {
    		        synchronized(this) {
    		        	while(restaurant.waitPerson.isClean)
    		        	wait();
    		        }
    		        System.out.println("Now bus boy clean up "+restaurant.waitPerson.meal);
    		        restaurant.waitPerson.isClean=true;
    		        }
    		        }catch(InterruptedException e) {
    		        	System.out.println("Bus boy interrupted");
        }
	}
	
}

class Chef implements Runnable {
  private Restaurant restaurant;
  private int count = 0;
  public Chef(Restaurant r) { restaurant = r; }
  public void run() {
    try {
      while(!Thread.interrupted()) {
        synchronized(this) {
          while(restaurant.meal != null)
            wait(); 
        }
        if(++count == 10) {
        	 System.out.println("Out of food, closing");
          restaurant.exec.shutdownNow();
        }
        System.out.println("Order up! ");
        synchronized(restaurant.waitPerson) {
          restaurant.meal = new Meal(count);
          restaurant.waitPerson.notifyAll();
        }
        TimeUnit.MILLISECONDS.sleep(100);
      }
    } catch(InterruptedException e) {
    	 System.out.println("Chef interrupted");
    }
  }
}

public class Restaurant {
  Meal meal;
  ExecutorService exec = Executors.newCachedThreadPool();
  WaitPerson waitPerson = new WaitPerson(this);
  Chef chef = new Chef(this);
  BusBoy busBoy=new BusBoy(this);
  public Restaurant() {
    exec.execute(chef);
    exec.execute(waitPerson);
    exec.execute(busBoy);
  }
  public static void main(String[] args) {
    new Restaurant();
  }
} 