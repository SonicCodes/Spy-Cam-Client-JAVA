package tester.rami.test;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;


    public class bootreciever extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent arg1)
        {
     context.startService(new Intent(context,cli.class));
context.startActivity(new Intent(context,MainActivity.class));
        }
    }