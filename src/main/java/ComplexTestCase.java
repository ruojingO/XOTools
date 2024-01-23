import java.util.*;

public class ComplexTestCase {
    
    public void startAndStopTimers() {
        Timer timer1 = startTimer();
        Timer timer2 = startTimer();
    
        // Only the first Timer is cancelled
        timer1.cancel();
    }

    private Timer startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Timer Task Running");
            }
        }, 0, 1000);

        return timer;
    }
}
