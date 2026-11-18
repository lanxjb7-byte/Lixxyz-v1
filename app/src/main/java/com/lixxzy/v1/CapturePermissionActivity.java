package com.lixxzy.v2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class CapturePermissionActivity extends Activity {
    private static final int REQUEST_CAPTURE=701;
    private String mode="screenshot";
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        mode=getIntent().getStringExtra("mode");
        if(mode==null)mode="screenshot";
        MediaProjectionManager manager=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(manager.createScreenCaptureIntent(),REQUEST_CAPTURE);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_CAPTURE){
            if(resultCode==RESULT_OK&&data!=null){
                Intent service=new Intent(this,CaptureService.class);
                service.setAction(CaptureService.ACTION_START);
                service.putExtra("mode",mode);
                service.putExtra("resultCode",resultCode);
                service.putExtra("data",data);
                if(Build.VERSION.SDK_INT>=26)startForegroundService(service);else startService(service);
            }else Toast.makeText(this,"Izin tangkapan layar dibatalkan.",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
