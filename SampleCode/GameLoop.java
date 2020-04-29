package Classes;

import Classes.app;

public class mainClass {
    public static void main(String[] args) {
        app a = new app();

        Boolean loop = true;

        long lastTimeRendered = System.nanoTime();

        long currentTime;

        long elapsedTime;

        final int DESIRED_FPS = 60;

        final double SECONDS_PER_FRAME = 1.0 / DESIRED_FPS;

        final double NANO_SECS_PER_FRAME = secondstoNanoSeconds(SECONDS_PER_FRAME);

        double actualFps;

        while (loop) 
        {
            a.h.updateAll();

            currentTime = System.nanoTime();

            elapsedTime = currentTime - lastTimeRendered;

            if(elapsedTime > java.lang.Double.valueOf(NANO_SECS_PER_FRAME).longValue())
            {
                a.h.renderAll();

                currentTime = System.nanoTime();

                elapsedTime = currentTime - lastTimeRendered;

                actualFps = 1 / nanoSecondsToSeconds(elapsedTime);

                System.out.println(actualFps);

                lastTimeRendered = System.nanoTime();

                loop = false;
            }
        }
    }

    public static double secondstoNanoSeconds(double seconds)
    {
        return seconds * Math.pow(10, 9);
    }

    public static double nanoSecondsToSeconds(long nanoSeconds)
    {
        return 1.0 * nanoSeconds * Math.pow(10, -9);
    }
}