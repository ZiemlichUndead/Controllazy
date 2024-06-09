package ml.ziemlichundead.controllazy;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.wear.ambient.AmbientModeSupport;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class MainActivity extends ComponentActivity implements MessageClient.OnMessageReceivedListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.TextID).setOnGenericMotionListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)) {
                // Don't forget the negation here
                float delta = -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL) * ViewConfigurationCompat.getScaledVerticalScrollFactor(ViewConfiguration.get(this), this);
                // Swap these axes to scroll horizontally instead
//                v.scrollBy(0, Math.round(delta));
//                System.out.println("Scroll delta:" +delta);
                sendVolToMobile(ev.getAxisValue(MotionEventCompat.AXIS_SCROLL) < 0);

                int vibrationlength = 5;
                    // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ((VibratorManager)getSystemService(Context.VIBRATOR_MANAGER_SERVICE)).getDefaultVibrator().vibrate(VibrationEffect.createOneShot(vibrationlength, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(vibrationlength);
                }

                return true;
            }
            return false;
        });
        findViewById(R.id.TextID).requestFocus();
        // Set an option to turn on lock task mode when starting the activity.
//        ActivityOptions options = ActivityOptions.makeBasic();
//        options.setLockTaskEnabled(true);
//        startLockTask();


        sendInit();

        Wearable.getMessageClient(this).addListener(this);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("Test",event.getRawX() +" | "+ event.getRawY());

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                event.getActionMasked() == MotionEvent.ACTION_MOVE ||
                event.getActionMasked() == MotionEvent.ACTION_UP) {

            // Send touch event data
            sendTouchMobile(event.getAction(), event.getX(), event.getY());

            return true; // Consume the event
        }
        return false;
//        return super.onTouchEvent(event);
    }


    private void sendTouchMobile(int action, float x, float y) {

        byte[] bytes = ByteBuffer.allocate(12).putInt(action).putFloat(x).putFloat(y).array();

        Wearable.getMessageClient(this).sendMessage("cly","/touch", bytes);

    }
    private void sendVolToMobile(boolean up) {
        byte[] bytes;

        if(up){
            bytes = ByteBuffer.allocate(1).put((byte)1).array();
        }else{
            bytes = ByteBuffer.allocate(1).put((byte)-1).array();
        }

        Wearable.getMessageClient(this).sendMessage("cly","/vol", bytes);

    }
    private void sendInit(){

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        byte[] bytes = ByteBuffer.allocate(12).putInt(metrics.widthPixels).array();
        Wearable.getMessageClient(this).sendMessage("cly","/init", bytes);
    }


    @Override
    public void onMessageReceived(@NonNull @NotNull MessageEvent messageEvent) {
//        ByteBuffer buffer = ByteBuffer.wrap(messageEvent.getData());
        if(messageEvent.getPath().equals("/clipboard")) {
            String receivedClipdata = new String(messageEvent.getData());
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if(clipboard.getPrimaryClip() != null) {
                clipboard.getPrimaryClip().addItem(new ClipData.Item(receivedClipdata));
            }else{
                clipboard.setText(receivedClipdata);
            }
            Toast.makeText(this,"Recieved clipboard data",Toast.LENGTH_SHORT).show();
        }
    }


}