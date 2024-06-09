package ml.ziemlichundead.controllazy;

import android.content.ClipboardManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button clipboardButton = findViewById(R.id.clipboardButton);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        clipboardButton.setOnClickListener(click -> {
            Wearable.getMessageClient(this).sendMessage("cly","/clipboard", clipboard.getPrimaryClip().getItemAt(0).getText().toString().getBytes());
            Toast.makeText(this,"Sent clipboard data",Toast.LENGTH_SHORT).show();
        });

    }


}