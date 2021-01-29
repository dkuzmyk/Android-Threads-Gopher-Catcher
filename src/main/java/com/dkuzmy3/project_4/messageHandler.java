package com.dkuzmy3.project_4;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class messageHandler extends Handler {
    @Override
    public void handleMessage(Message msg){
        if(msg.what == 1){
            Log.i("handleMessage", "message sent and received");}
    }

}
