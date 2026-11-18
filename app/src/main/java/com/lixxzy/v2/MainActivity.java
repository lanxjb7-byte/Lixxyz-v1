package com.lixxzy.v2;

import android.app.*;
import android.os.*;
import android.content.*;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import java.util.*;

public class MainActivity extends Activity {
    private static final String ACCESS_KEY = "LAX-YSK-02HA";
    private static final String PREF = "lixzzy_session";
    private final int BG=Color.rgb(11,8,10), PANEL=Color.rgb(29,17,21), RED=Color.rgb(255,48,77);
    private LinearLayout root;

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);showLogin();}
    @Override protected void onResume(){super.onResume(); if(getSharedPreferences(PREF,0).getBoolean("sessionEnded",false)){getSharedPreferences(PREF,0).edit().putBoolean("sessionEnded",false).apply();showLogin();}}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int c,int s){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(16));if(s!=0)d.setStroke(dp(1),s);return d;}
    private TextView text(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);if(b)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(22),dp(20),dp(22),dp(20));l.setBackgroundColor(BG);return l;}
    private void base(LinearLayout l){ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(l);setContentView(sc);}
    private TextView button(String s){TextView b=text(s,15,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(14),dp(15),dp(14),dp(15));b.setBackground(shape(RED,0));return b;}
    private void showLogin(){
        root=column();root.setGravity(Gravity.CENTER);
        TextView logo=text("Lixzzy",34,RED,true);logo.setGravity(Gravity.CENTER);root.addView(logo);
        TextView sub=text("V1  •  FLOATING GAME TOOL",11,Color.LTGRAY,true);sub.setGravity(Gravity.CENTER);root.addView(sub);
        Space gap=new Space(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(35)));
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(18),dp(20),dp(18),dp(20));card.setBackground(shape(PANEL,0xff682131));
        card.addView(text("LOGIN ACCESS",18,Color.WHITE,true));
        EditText key=new EditText(this);key.setSingleLine(true);key.setHint("Masukkan access key");key.setHintTextColor(0xffa99ba0);key.setTextColor(Color.WHITE);
        key.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);key.setPadding(dp(12),dp(12),dp(12),dp(12));key.setBackground(shape(0xff170e11,0xff743044));
        LinearLayout.LayoutParams kp=new LinearLayout.LayoutParams(-1,dp(52));kp.topMargin=dp(16);card.addView(key,kp);
        TextView login=button("LOGIN");LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(50));bp.topMargin=dp(16);card.addView(login,bp);
        TextView error=text("",12,0xffff6b7e,false);card.addView(error);
        login.setOnClickListener(v->{if(ACCESS_KEY.equals(key.getText().toString().trim())){getSharedPreferences(PREF,0).edit().putBoolean("sessionEnded",false).apply();showDashboard();}else error.setText("Access key salah.");});
        root.addView(card,new LinearLayout.LayoutParams(-1,-2));
        TextView note=text("Akses lokal demo • key tertanam di APK",10,0xff887b80,false);note.setGravity(Gravity.CENTER);root.addView(note);base(root);
    }
    private void addFeature(LinearLayout parent,String name,String percent){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),dp(12),dp(12),dp(12));row.setBackground(shape(PANEL,0xff4d202c));
        LinearLayout info=new LinearLayout(this);info.setOrientation(1);info.addView(text(name,15,Color.WHITE,true));info.addView(text(percent,12,RED,true));row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this);sw.setText("OFF");sw.setTextColor(Color.WHITE);sw.setOnCheckedChangeListener((v,on)->sw.setText(on?"ON":"OFF"));row.addView(sw);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(10);parent.addView(row,p);
    }
    private boolean usageAllowed(){
        try{UsageStatsManager u=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);long now=System.currentTimeMillis();return u.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,now-60000,now).size()>0;}catch(Exception e){return false;}
    }
    private void launchGame(String pkg){
        if(!Settings.canDrawOverlays(this)){new AlertDialog.Builder(this).setTitle("Izin floating window").setMessage("Izinkan Lixzzy tampil di atas aplikasi lain, lalu kembali dan tekan START lagi.").setPositiveButton("BUKA IZIN",(d,w)->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())))).setNegativeButton("BATAL",null).show();return;}
        if(!usageAllowed()){new AlertDialog.Builder(this).setTitle("Izin deteksi game").setMessage("Aktifkan Usage Access untuk mendeteksi saat Free Fire ditutup. Setelah memberi izin, kembali dan tekan START lagi.").setPositiveButton("BUKA PENGATURAN",(d,w)->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))).setNegativeButton("BATAL",null).show();return;}
        Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
        if(launch==null){Toast.makeText(this,"Game belum terpasang.",Toast.LENGTH_LONG).show();return;}
        Intent service=new Intent(this,FloatingService.class);service.putExtra("target",pkg);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(service);else startService(service);
        new Handler().postDelayed(()->{try{startActivity(launch);}catch(Exception e){Toast.makeText(this,"Tidak dapat membuka game.",Toast.LENGTH_SHORT).show();}},350);
    }
    private void showDashboard(){
        root=column();TextView brand=text("LIXXZY V2",25,RED,true);brand.setGravity(Gravity.CENTER);root.addView(brand);
        TextView status=text("ACCESS GRANTED • FLOATING PANEL",11,0xffb8aeb2,true);status.setGravity(Gravity.CENTER);root.addView(status);
        TextView section=text("FEATURE CONTROL • DISPLAY ONLY",13,Color.WHITE,true);LinearLayout.LayoutParams sec=new LinearLayout.LayoutParams(-1,-2);sec.topMargin=dp(18);root.addView(section,sec);
        addFeature(root,"DRAG HS","40%");addFeature(root,"HEADLOCK","30%");addFeature(root,"AIM PERFECT","10%");
        TextView permission=button("CHECK / ENABLE FLOATING PERMISSIONS");LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(50));pp.topMargin=dp(22);root.addView(permission,pp);
        permission.setOnClickListener(v->{if(!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())));else if(!usageAllowed())startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));else Toast.makeText(this,"Izin sudah aktif.",Toast.LENGTH_SHORT).show();});
        TextView ff=button("🔥  START FREE FIRE");LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(54));fp.topMargin=dp(12);root.addView(ff,fp);ff.setOnClickListener(v->launchGame("com.dts.freefireth"));
        TextView max=button("🔥  START FREE FIRE MAX");LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(54));mp.topMargin=dp(10);root.addView(max,mp);max.setOnClickListener(v->launchGame("com.dts.freefiremax"));
        TextView desc=text("Floating panel hanya berisi utilitas dan shortcut. Switch fitur tidak mengubah aim/gameplay.",10,0xff9d8f94,false);desc.setGravity(Gravity.CENTER);desc.setPadding(0,dp(18),0,dp(10));root.addView(desc);
        TextView logout=text("LOG OUT",12,RED,true);logout.setGravity(Gravity.CENTER);logout.setPadding(0,dp(12),0,dp(8));root.addView(logout);logout.setOnClickListener(v->{stopService(new Intent(this,FloatingService.class));showLogin();});
        base(root);
    }
}