package ml.ziemlichundead.controllazy;

import android.accessibilityservice.AccessibilityService;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.graphics.Path;
import android.media.AudioManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

public class MyAccessibilityService extends AccessibilityService implements MessageClient.OnMessageReceivedListener{


    @Override
    public void onCreate() {
        super.onCreate();
        Wearable.getMessageClient(this).addListener(this);
//        setServiceInfo(AccessibilityServiceInfo.);

        Log.i("Debug","Service Oncreate");
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull MessageEvent messageEvent) {

        ByteBuffer buffer = ByteBuffer.wrap(messageEvent.getData());
        if(messageEvent.getPath().equals("/touch")) {
            newCircleToRectangle(buffer.getInt(), buffer.getFloat(), buffer.getFloat());
        }else if (messageEvent.getPath().equals("/vol")){
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustVolume(buffer.get(),AudioManager.FLAG_SHOW_UI);
        }else if (messageEvent.getPath().equals("/init")){
            CIRCLE_DIAMETER = buffer.getInt();
            System.out.println("Received circle width: "+CIRCLE_DIAMETER);
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d("Debug", "onAccessibilityEvent: " + accessibilityEvent.getEventType());
    }

    @Override
    public void onInterrupt() {
        Log.e("Debug", "onInterrupt: something went wrong");
    }


//    GestureDescription.Builder gesture;
//    GestureDescription.StrokeDescription lastStroke;
//    long touchStart;
//    long lastTouch;
//
//    float lastX, lastY = -1;
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void executeTouch(int action, float x, float y){
//
//        long currentTime = System.currentTimeMillis();
//
//        if(action == MotionEvent.ACTION_DOWN){
//            touchStart = currentTime;
//            gesture = new GestureDescription.Builder();
//            lastStroke = null;
////            lastX = -1;
////            lastY = -1;
//        }
//
//        if(action == MotionEvent.ACTION_MOVE){
//
//            if(lastStroke == null) {
//                Path firstPath = new Path();
//                firstPath.moveTo(lastX, lastY);
//                lastStroke = new GestureDescription.StrokeDescription(firstPath, 0, currentTime - touchStart, true);
//                gesture.addStroke(lastStroke);
//            }
//            Path path = new Path();
//            path.moveTo(lastX,lastY);
//            path.lineTo(x,y);
//
//            GestureDescription.StrokeDescription newStroke = lastStroke.continueStroke(path, lastTouch - touchStart, currentTime - lastTouch+1, true);
//
//            gesture.addStroke(newStroke);
//            lastStroke = newStroke;
//
//        }
//
//        if(action == MotionEvent.ACTION_UP) {
//
//
//            if(lastStroke == null){
//                Path path = new Path();
//                path.moveTo(lastX,lastY);
//                GestureDescription.StrokeDescription firstStroke = new GestureDescription.StrokeDescription(path, 0, currentTime - touchStart+1, true);
//                gesture.addStroke(firstStroke);
//            }
//
//
//            GestureDescription gestureDescription = gesture.build();
//            for (int i = 0; i < gestureDescription.getStrokeCount(); i++) {
//                System.out.println(gestureDescription.getStroke(0).getPath());
//            }
//            dispatchGesture(gestureDescription,null,null);
//        }
//
//
//        lastX = x;
//        lastY = y;
//        lastTouch = currentTime;
//
//    }

    Path currentPath;
    long touchStart;
    int[] lastTouch = new int[2];
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void executeTouch(int action, float x, float y){
//        if(action == MotionEvent.ACTION_DOWN){
//            currentPath = new Path();
//            currentPath.moveTo(x,y);
//            touchStart = System.currentTimeMillis();
//        }else if(action == MotionEvent.ACTION_MOVE){
//            currentPath.lineTo(x,y);
//        }else if(action == MotionEvent.ACTION_UP){
//            dispatchGesture(new GestureDescription.Builder()
//                    .addStroke(new GestureDescription.StrokeDescription(currentPath, 0, System.currentTimeMillis()-touchStart+1)).build(), null, null);
//        }

        if(action == MotionEvent.ACTION_DOWN){
            touchStart = System.currentTimeMillis();
            lastTouch[0] = (int) x;
            lastTouch[1] = (int) y;
        }else if(action == MotionEvent.ACTION_MOVE){
//            executeRootSwipe(lastTouch[0],lastTouch[1], (int) x, (int) y,System.currentTimeMillis()-touchStart);
//            lastTouch[0] = (int) x;
//            lastTouch[1] = (int) y;

        }else if(action == MotionEvent.ACTION_UP){

//            if(lastTouch[0] == (int) x && lastTouch[1] == (int) y){
//                executeRootTap( (int) x, (int) y, System.currentTimeMillis()-touchStart);
            executeRootLowLevelAction(action, (int) x, (int) y);
//            }else{
//                executeRootSwipe(lastTouch[0],lastTouch[1], (int) x, (int) y,System.currentTimeMillis()-touchStart);
//            }
        }

    }

    public void executeRootTap(int x, int y, long duration){
        executeRootCommand("input tap "+x+" "+y+" "+duration);
        System.out.println("Touch at: "+x+" "+y);
    }

    public void executeRootSwipe(int x, int y, int x2, int y2, long duration){
        executeRootCommand("input swipe "+x+" "+y+ " "+x2 + " "+ y2+" "+duration);
        System.out.println("Swipe from: "+x+" "+y+" to: "+x2+" "+y2);
    }
    int number = 0;
    public void executeRootLowLevelAction(int action, int x, int y){
        number++;
        String txt = "";
        if(action == 0) { //Down
            txt =   "sendevent /dev/input/event5 3 39 "+number+"\n" +
                    "sendevent /dev/input/event5 1 330 1\n" +
                    "sendevent /dev/input/event5 3 35 "+x+"\n" +
                    "sendevent /dev/input/event5 3 36 "+y+"\n" +
                    "sendevent /dev/input/event5 0 0 0";
        }else if(action == 1) {
            txt =   "sendevent /dev/input/event5 3 35 "+x+"\n" +
                    "sendevent /dev/input/event5 3 36 "+y+"\n" +
                    "sendevent /dev/input/event5 0 0 0";

        }else if(action == 2) {
            txt =   "sendevent /dev/input/event5 3 39 4294967295\n" +
                    "sendevent /dev/input/event5 1 330 0\n" +
                    "sendevent /dev/input/event5 0 0 0";
        }else {
//
//            txt =           "sendevent /dev/input/event0 3 57 0\n" +
//                    "sendevent /dev/input/event0 3 53 "+x+"\n" +
//                    "sendevent /dev/input/event0 3 54 "+y+"\n" +
//                    "sendevent /dev/input/event0 3 48 5\n" +
//                    "sendevent /dev/input/event0 3 58 50\n" +
//                    "sendevent /dev/input/event0 0 2 0\n" +
//                    "sendevent /dev/input/event0 0 0 0";
        }

        executeRootCommand(txt);
    }
    public void executeRootCommand(String cmd){
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

//            outputStream.writeBytes("input swipe X1 Y1 X2 Y2");
            outputStream.writeBytes(cmd+"\n");
            outputStream.flush();
        }catch(Exception ignored){
        }
    }

    public void newCircleToRectangle(int status, float x, float y){
        float diameter = CIRCLE_DIAMETER-OFFSET;

        //Pull to middle
        x -= diameter /2;
        y -= diameter /2;

//        //Normalize coordinates
//        x /= (diameter/2);
//        y /= (diameter/2);

        float normalizedDistance = Math.min((float) Math.sqrt(x * x + y * y) / (diameter/2), 1.0f);

        double angle = Math.atan2(y,x);

        System.out.println("Angle:" +angle);
        System.out.println("Distance:" +normalizedDistance);

        Vector<Float> screenDims = getScreenDimensions();
        double screenWidth = screenDims.get(0);
        double screenHeight = screenDims.get(1);

        double calcAngle = Math.abs(angle);
        boolean flipped = false;
        if(calcAngle > Math.PI/2){
            calcAngle -= Math.PI/2;
            flipped = true;
        }
        double xDistance;
        double yDistance;
        if(!flipped) {
            xDistance = (screenHeight / 2) / Math.tan(calcAngle);
            yDistance = Math.tan(calcAngle) * (screenWidth / 2);
        }else{
            yDistance = (screenWidth / 2) / Math.tan(calcAngle);
            xDistance = Math.tan(calcAngle) * (screenHeight / 2);
        }

        Vector<Double> vectorToEdge = new Vector<>();

        System.out.println("xD: "+xDistance);
        System.out.println("yD: "+yDistance);

        if(xDistance > screenWidth/2){
            vectorToEdge.add(screenWidth/2d);
            vectorToEdge.add(yDistance);
        }else if(yDistance > screenHeight/2){
            vectorToEdge.add(xDistance);
            vectorToEdge.add(screenHeight/2d);
        }else{
            vectorToEdge.add(screenWidth/2d);
            vectorToEdge.add(screenHeight/2d);
        }


        Vector<Float> vectorToPoint = new Vector<>();
//        vectorToPoint.add((float) (vectorToEdge.get(0)*normalizedDistance + screenWidth/2));
        if(Math.abs(angle) < Math.PI/2){
            vectorToPoint.add((float) (vectorToEdge.get(0)*normalizedDistance + screenWidth/2));
        }else{
            vectorToPoint.add((float) (screenWidth/2 - vectorToEdge.get(0)*normalizedDistance ));
        }

        if(angle > 0){
            vectorToPoint.add((float) (vectorToEdge.get(1)*normalizedDistance + screenHeight/2));
        }else{
            vectorToPoint.add((float) (screenHeight/2 - vectorToEdge.get(1)*normalizedDistance));
        }

        System.out.println("Point Vector: "+vectorToPoint.get(0)+" | "+vectorToPoint.get(1));


        executeTouch(status,vectorToPoint.get(0),vectorToPoint.get(1));




    }


    public static float CIRCLE_DIAMETER = 450;
    public static float OFFSET = 20;

    public void circleToRectangleCoordinates(int status, float x, float y){


        x -= OFFSET;
        y -= OFFSET;

        float diameter = CIRCLE_DIAMETER - (2*OFFSET);

        //Pull to middle
        x -= diameter /2;
        y -= diameter /2;


        //Normalize coordinates
        x /= (diameter/2);
        y /= (diameter/2);

        System.out.println("WATCH X: "+x +" | Y: "+y);

        double distance = Math.sqrt(x * x + y * y);
        if(distance > 1){
            System.out.println(distance);
        }


        Vector<Float> v = ellipticalDiscToSquare(x,y);

//        System.out.println("X: "+x +" | Y: "+y);
//        System.out.println("Vector: "+v);


        Vector<Float> screenDims = getScreenDimensions();
        float recX = v.get(0) * (screenDims.get(0)/2);
        float recY = v.get(1) * (screenDims.get(1)/2);

        recX += screenDims.get(0) /2;
        recY += screenDims.get(1) /2;

        recX = Math.min(Math.max(recX, 0), screenDims.get(0));
        recY = Math.min(Math.max(recY, 0), screenDims.get(1));
        System.out.println("X: "+recX +" | Y: "+recY);
        if(Float.isNaN(recX) || Float.isNaN(recY)){
            return;
        }
        executeTouch(status,recX,recY);
    }

    public Vector<Float> getScreenDimensions(){
        Display display;
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Vector<Float> vec = new Vector<>();
        vec.add((float) metrics.widthPixels);
        vec.add((float) metrics.heightPixels);
        return vec;
    }
    static Vector<Float> ellipticalDiscToSquare(float u, float v)
    {
//        System.out.println("Before X: "+u);
//        System.out.println("Before Y: "+v);
        double u2 = u * u;
        double v2 = v * v;
        double twosqrt2 = 2.0 * Math.sqrt(2.0);
        double subtermx = 2.0 + u2 - v2;
        double subtermy = 2.0 - u2 + v2;
        double termx1 = subtermx + u * twosqrt2;
        double termx2 = subtermx - u * twosqrt2;
        double termy1 = subtermy + v * twosqrt2;
        double termy2 = subtermy - v * twosqrt2;
        float x = (float) (0.5 * Math.sqrt(termx1) - 0.5 * Math.sqrt(termx2));
        float y = (float) (0.5 * Math.sqrt(termy1) - 0.5 * Math.sqrt(termy2));

//        System.out.println("X: "+x);
//        System.out.println("Y: "+y);
        Vector<Float> vec = new Vector();
        vec.add(x);
        vec.add(y);
        return vec;

    }

}
