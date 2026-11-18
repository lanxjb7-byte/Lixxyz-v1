package com.lixzzy.v1;

import android.app.*;
import android.app.usage.*;
import android.app.ActivityManager;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class FloatingService extends Service {
    private static final String CHANNEL="lixzzy_float";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WindowManager wm;
    private View bubble,panel;
    private WindowManager.LayoutParams bubbleParams,panelParams;
    private String target="";
    private boolean sawGame=false, expanded=false, stopped=false;
    private String gameMode="SEIMBANG";
    private int themeAccent(){return "RINGAN".equals(gameMode)?0xff42e66b:("EKSTREM".equals(gameMode)?0xffff304d:0xff35a7ff);}
    private int themePanel(){return "RINGAN".equals(gameMode)?0xff071b10:("EKSTREM".equals(gameMode)?0xff16090d:0xff091522);}
    private int themeButton(){return "RINGAN".equals(gameMode)?0xff123a22:("EKSTREM".equals(gameMode)?0xff32101a:0xff12304a);}
    private Runnable checker;

    @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);gameMode=getSharedPreferences("lixzzy_session",0).getString("gameMode","SEIMBANG");createChannel();}
    @Override public int onStartCommand(Intent intent,int flags,int id){
        target=intent==null?"com.dts.freefireth":intent.getStringExtra("target");
        if(target==null)target="com.dts.freefireth";
        startForeground(41,notification());
        checker=()->checkForeground();
        handler.postDelayed(checker,1000);
        return START_NOT_STICKY;
    }
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"Lixzzy Floating Panel",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    private Notification notification(){
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        return b.setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Lixzzy V1 aktif").setContentText("Menunggu Free Fire / Free Fire MAX").setOngoing(true).build();
    }
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private WindowManager.LayoutParams params(int w,int h,int gravity){
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        return new WindowManager.LayoutParams(w,h,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
    }
    private void showBubble(){
        if(bubble!=null||!Settings.canDrawOverlays(this))return;
        ImageView icon=new ImageView(this);icon.setImageResource(com.lixzzy.v1.R.mipmap.ic_launcher);icon.setScaleType(ImageView.ScaleType.CENTER_CROP);icon.setBackground(bg(Color.BLACK,0xffff304d));icon.setPadding(dp(2),dp(2),dp(2),dp(2));
        bubble=icon;bubbleParams=params(dp(58),dp(58),Gravity.TOP|Gravity.LEFT);bubbleParams.gravity=Gravity.TOP|Gravity.LEFT;bubbleParams.x=dp(12);bubbleParams.y=dp(180);
        icon.setOnClickListener(v->{if(expanded)hidePanel();else showPanel();});
        icon.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;boolean moved;
            public boolean onTouch(View v,android.view.MotionEvent e){switch(e.getAction()){case MotionEvent.ACTION_DOWN:sx=e.getRawX();sy=e.getRawY();ox=bubbleParams.x;oy=bubbleParams.y;moved=false;return true;case MotionEvent.ACTION_MOVE:int dx=(int)(e.getRawX()-sx),dy=(int)(e.getRawY()-sy);if(Math.abs(dx)+Math.abs(dy)>dp(5))moved=true;bubbleParams.x=ox+dx;bubbleParams.y=oy+dy;try{wm.updateViewLayout(bubble,bubbleParams);}catch(Exception ex){}return true;case MotionEvent.ACTION_UP:if(!moved)v.performClick();return true;}return false;}
        });
        try{wm.addView(bubble,bubbleParams);}catch(Exception e){bubble=null;}
    }
    private void showPanel(){
        if(panel!=null)return;
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackground(bg(themePanel(),themeAccent()));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(txt("Lixzzy V1 • "+gameMode,17,themeAccent(),true),new LinearLayout.LayoutParams(0,-2,1));
        TextView close=txt("✕",20,Color.WHITE,true);head.addView(close);content.addView(head);close.setOnClickListener(v->hidePanel());
        addPanelButton(content,"MODE SENYAP • NOTIFIKASI / TELPON",()->toggleSilentMode());
        addPanelButton(content,"GAME SPACE • PILIH MODE",()->showGameModes());
        addPanelButton(content,"SCREENSHOT",()->openCapture("screenshot"));
        addPanelButton(content,"RECORD SCREEN",()->openCapture("record"));
        addPanelButton(content,"STOP RECORD",()->stopCapture());
        addPanelButton(content,"PEMBERSIHAN MEMORI",()->showMemoryInfo());
        addPanelButton(content,"TAMBAHKAN APK / APLIKASI",()->showAppPicker(true));
        addPanelButton(content,"OPEN APLIKASI KE-2",()->showAppPicker(false));
        addPanelButton(content,"QUICK SETTINGS",()->openSettings());
        addPanelButton(content,"NYALAKAN WIFI",()->openNetworkPanel("wifi"));
        addPanelButton(content,"DATA SELULER",()->openNetworkPanel("data"));
        addPanelButton(content,"HOTSPOT",()->openNetworkPanel("hotspot"));
        addPanelButton(content,"BLUETOOTH",()->openNetworkPanel("bluetooth"));
        addPanelButton(content,"TIMER 1 MENIT",()->{toast("Timer 1 menit dimulai.");new Handler(Looper.getMainLooper()).postDelayed(()->toast("Timer selesai."),60000);});
        addPanelButton(content,"CATATAN",()->showNotes());
        addPanelButton(content,"DRAG HS • 40% [DISPLAY]",()->toast("Kontrol tampilan saja."));
        addPanelButton(content,"HEADLOCK • 30% [DISPLAY]",()->toast("Kontrol tampilan saja."));
        addPanelButton(content,"AIM PERFECT • 10% [DISPLAY]",()->toast("Kontrol tampilan saja."));
        addPinnedApps(content);
        TextView footer=txt("Keluar dari game = panel ditutup",10,0xffb7a8ad,false);footer.setPadding(0,dp(8),0,0);content.addView(footer);
        scroll.addView(content);box.addView(scroll);
        panel=box;panelParams=params(dp(270),dp(440),Gravity.TOP|Gravity.LEFT);panelParams.gravity=Gravity.TOP|Gravity.LEFT;panelParams.x=dp(78);panelParams.y=dp(110);
        try{wm.addView(panel,panelParams);expanded=true;}catch(Exception e){panel=null;toast("Tidak bisa menampilkan panel di atas aplikasi ini.");}
    }
    private void addPanelButton(LinearLayout box,String label,Runnable action){
        TextView b=txt(label,12,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(8),dp(11),dp(8),dp(11));b.setBackground(bg(themeButton(),themeAccent()));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(7);box.addView(b,p);b.setOnClickListener(v->action.run());
    }

    private void toggleSilentMode(){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=23 && !nm.isNotificationPolicyAccessGranted()){
            try{Intent i=new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);toast("Izinkan akses Jangan Ganggu untuk mengaktifkan Mode Senyap.");}
            catch(Exception e){toast("Buka Pengaturan > Suara > Jangan Ganggu secara manual.");}
            return;
        }
        try{
            boolean active=Build.VERSION.SDK_INT>=23 && nm.getCurrentInterruptionFilter()==NotificationManager.INTERRUPTION_FILTER_NONE;
            nm.setInterruptionFilter(active?NotificationManager.INTERRUPTION_FILTER_ALL:NotificationManager.INTERRUPTION_FILTER_NONE);
            toast(active?"Mode Senyap dimatikan.":"Mode Senyap aktif. Panggilan dan notifikasi dibisukan sesuai aturan Jangan Ganggu.");
            hidePanel();showPanel();
        }catch(Exception e){toast("Mode Senyap tidak dapat diubah. Periksa izin Jangan Ganggu.");}
    }
    private void showGameModes(){
        String[] modes={"RINGAN","SEIMBANG","EKSTREM"};
        String[] details={"Hijau • tema ringan","Biru • tema seimbang","Merah-hitam • tema ekstrem"};
        AlertDialog d=new AlertDialog.Builder(this).setTitle("GAME SPACE • MODE TAMPILAN").setItems(new String[]{details[0],details[1],details[2]},(dialog,which)->{
            gameMode=modes[which];getSharedPreferences("lixzzy_session",0).edit().putString("gameMode",gameMode).apply();
            hidePanel();showPanel();toast("Tema jendela mengambang: "+gameMode);
        }).setNegativeButton("BATAL",null).create();
        if(Build.VERSION.SDK_INT>=26)d.getWindow();
        d.getWindow().setType(Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE);
        d.show();
    }

    private void openCapture(String mode){
        Intent i=new Intent(this,CapturePermissionActivity.class);i.putExtra("mode",mode);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);
    }
    private void stopCapture(){
        Intent i=new Intent(this,CaptureService.class);i.setAction(CaptureService.ACTION_STOP);
        startService(i);
        toast("Perintah stop recording dikirim.");
    }
    private void showMemoryInfo(){
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();am.getMemoryInfo(mi);
        long total=mi.totalMem/(1024*1024), avail=mi.availMem/(1024*1024), used=total-avail;
        String msg="RAM total: "+total+" MB\nRAM terpakai sistem/aplikasi: "+used+" MB\nRAM tersedia: "+avail+" MB\n\nAndroid mengelola RAM otomatis. Lixzzy tidak dapat memaksa membersihkan memori Free Fire.";
        AlertDialog memoryDialog = new AlertDialog.Builder(this).setTitle("PEMBERSIHAN MEMORI").setMessage(msg).setPositiveButton("PENGATURAN APLIKASI",(d,w)->{
            try{Intent x=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+target));x.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(x);}catch(Exception e){openSettings();}
        }).setNegativeButton("TUTUP",null).create();
        if(Build.VERSION.SDK_INT>=26)memoryDialog.getWindow();
        memoryDialog.getWindow().setType(Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE);
        memoryDialog.show();
    }
    private void openSettings(){try{Intent i=new Intent(Settings.ACTION_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception e){toast("Pengaturan tidak bisa dibuka.");}}
    private void openNetworkPanel(String kind){
        try{
            Intent intent;
            if("wifi".equals(kind)){
                if(Build.VERSION.SDK_INT>=29){
                    intent=new Intent(Settings.Panel.ACTION_WIFI);
                }else{
                    intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                }
            }else if("data".equals(kind)){
                if(Build.VERSION.SDK_INT>=29){
                    intent=new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                }else{
                    intent=new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                }
            }else if("hotspot".equals(kind)){
                intent=new Intent("android.settings.TETHER_SETTINGS");
                if(intent.resolveActivity(getPackageManager())==null)
                    intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            }else if("bluetooth".equals(kind)){
                if(Build.VERSION.SDK_INT>=33){
                    intent=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                }else{
                    intent=new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
                }
            }else{
                intent=new Intent(Settings.ACTION_SETTINGS);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch(Exception e){
            try{
                Intent fallback=new Intent(Settings.ACTION_SETTINGS);
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
            }catch(Exception ignored){
                toast("Pengaturan jaringan tidak dapat dibuka di perangkat ini.");
            }
        }
    }
    private void showNotes(){EditText e=new EditText(this);e.setText(getSharedPreferences("lixzzy_notes",0).getString("note",""));AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Catatan").setView(e).setPositiveButton("SIMPAN",(d,w)->getSharedPreferences("lixzzy_notes",0).edit().putString("note",e.getText().toString()).apply()).setNegativeButton("TUTUP",null).create();if(Build.VERSION.SDK_INT>=26)dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);dialog.show();}
    private List<ResolveInfo> getLaunchableApps(){
        Intent query=new Intent(Intent.ACTION_MAIN);query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=getPackageManager().queryIntentActivities(query,0);
        Collections.sort(apps,(a,b)->String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(String.valueOf(b.loadLabel(getPackageManager()))));
        return apps;
    }
    private void showAppPicker(boolean pin){
        List<ResolveInfo> apps=getLaunchableApps();ArrayList<String> labels=new ArrayList<>();ArrayList<Intent> intents=new ArrayList<>();ArrayList<String> packages=new ArrayList<>();
        for(ResolveInfo r:apps){String p=r.activityInfo.packageName;if(p.equals(getPackageName())||p.equals(target))continue;labels.add(String.valueOf(r.loadLabel(getPackageManager())));packages.add(p);Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);i.setClassName(p,r.activityInfo.name);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);intents.add(i);}
        if(labels.isEmpty()){toast("Tidak ada aplikasi lain ditemukan.");return;}
        AlertDialog picker=new AlertDialog.Builder(this).setTitle(pin?"TAMBAHKAN APK KE PANEL":"OPEN APLIKASI KE-2").setItems(labels.toArray(new String[0]),(d,which)->{
            if(pin){Set<String> set=new HashSet<>(getSharedPreferences("lixzzy_pins",0).getStringSet("apps",new HashSet<>()));set.add(packages.get(which));getSharedPreferences("lixzzy_pins",0).edit().putStringSet("apps",set).apply();hidePanel();showPanel();toast("Shortcut ditambahkan ke panel.");}
            else try{startActivity(intents.get(which));}catch(Exception ex){toast("Aplikasi tidak bisa dibuka.");}
        }).setNegativeButton("BATAL",null).create();
        if(Build.VERSION.SDK_INT>=26)picker.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        picker.show();
    }
    private void addPinnedApps(LinearLayout content){
        Set<String> pins=getSharedPreferences("lixzzy_pins",0).getStringSet("apps",new HashSet<>());
        if(pins.isEmpty())return;
        TextView title=txt("APK / APLIKASI FAVORIT",11,0xffff5268,true);title.setPadding(0,dp(12),0,0);content.addView(title);
        for(String pkg:new HashSet<>(pins)){
            try{
                android.content.pm.ApplicationInfo ai=getPackageManager().getApplicationInfo(pkg,0);
                String label=getPackageManager().getApplicationLabel(ai).toString();
                addPanelButton(content,"BUKA • "+label,()->{try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i!=null){i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}else toast("Aplikasi tidak bisa dibuka.");}catch(Exception e){toast("Gagal membuka aplikasi.");}});
            }catch(Exception ignored){}
        }
    }
    private void hidePanel(){if(panel!=null){try{wm.removeView(panel);}catch(Exception e){}panel=null;}expanded=false;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void checkForeground(){
        if(stopped)return;
        String top=topPackage();
        if(target.equals(top)){sawGame=true;if(bubble==null)showBubble();}
        else if(sawGame){getSharedPreferences("lixzzy_session",0).edit().putBoolean("sessionEnded",true).apply();stopSelf();return;}
        handler.postDelayed(checker,1200);
    }
    private String topPackage(){
        try{UsageStatsManager us=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);long now=System.currentTimeMillis();List<UsageStats> list=us.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,now-15000,now);UsageStats latest=null;if(list!=null)for(UsageStats u:list)if(latest==null||u.getLastTimeUsed()>latest.getLastTimeUsed())latest=u;return latest==null?"":latest.getPackageName();}catch(Exception e){return "";}
    }
    @Override public void onDestroy(){stopped=true;handler.removeCallbacksAndMessages(null);hidePanel();if(bubble!=null){try{wm.removeView(bubble);}catch(Exception e){}bubble=null;}super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
