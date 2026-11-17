package com.lixxzy.v1;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private static final String ACCESS_KEY = "LAX-YSK-02HA";
    private final int BG = Color.rgb(11,8,10);
    private final int PANEL = Color.rgb(29,17,21);
    private final int RED = Color.rgb(255,48,77);
    private LinearLayout root;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showLogin();
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(16));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(22),dp(20),dp(22),dp(20)); l.setBackgroundColor(BG);
        return l;
    }
    private void base(LinearLayout l) {
        ScrollView sc = new ScrollView(this); sc.setFillViewport(true); sc.addView(l); setContentView(sc);
    }
    private TextView button(String label) {
        TextView b = text(label,15,Color.WHITE,true); b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14),dp(15),dp(14),dp(15)); b.setBackground(shape(RED,0));
        return b;
    }
    private void showLogin() {
        root = column(); root.setGravity(Gravity.CENTER);
        TextView logo = text("Lixzzy",34,RED,true); logo.setGravity(Gravity.CENTER);
        root.addView(logo);
        TextView sub = text("LIXXY MODS  •  LIX",11,Color.LTGRAY,true);
        sub.setGravity(Gravity.CENTER); root.addView(sub);
        Space gap = new Space(this); root.addView(gap,new LinearLayout.LayoutParams(1,dp(35)));
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18),dp(20),dp(18),dp(20)); card.setBackground(shape(PANEL,0xff682131));
        TextView title=text("LOGIN ACCESS",18,Color.WHITE,true); card.addView(title);
        EditText key = new EditText(this); key.setSingleLine(true); key.setHint("Masukkan access key");
        key.setHintTextColor(0xffa99ba0); key.setTextColor(Color.WHITE);
        key.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        key.setPadding(dp(12),dp(12),dp(12),dp(12)); key.setBackground(shape(0xff170e11,0xff743044));
        LinearLayout.LayoutParams kp=new LinearLayout.LayoutParams(-1,dp(52)); kp.topMargin=dp(16); card.addView(key,kp);
        TextView login=button("LOGIN"); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(50)); bp.topMargin=dp(16); card.addView(login,bp);
        TextView error=text("",12,0xffff6b7e,false); error.setPadding(0,dp(10),0,0); card.addView(error);
        login.setOnClickListener(v -> {
            if (ACCESS_KEY.equals(key.getText().toString().trim())) showDashboard();
            else error.setText("Access key salah. Coba lagi.");
        });
        root.addView(card,new LinearLayout.LayoutParams(-1,-2));
        TextView note=text("Akses demo lokal • key ditanam di aplikasi",10,0xff887b80,false);
        note.setGravity(Gravity.CENTER); LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2); np.topMargin=dp(16); root.addView(note,np);
        base(root);
    }
    private void addFeature(LinearLayout parent, String name, String percent) {
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14),dp(12),dp(12),dp(12)); row.setBackground(shape(PANEL,0xff4d202c));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
        TextView n=text(name,15,Color.WHITE,true); info.addView(n);
        TextView p=text(percent,12,RED,true); info.addView(p);
        row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this); sw.setText("OFF"); sw.setTextColor(Color.WHITE);
        sw.setOnCheckedChangeListener((buttonView, checked)->sw.setText(checked?"ON":"OFF"));
        row.addView(sw);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.topMargin=dp(11); parent.addView(row,rp);
    }
    private void launchGame(String pkg) {
        Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null) startActivity(launch);
        else Toast.makeText(this,"Game belum terpasang di perangkat.",Toast.LENGTH_LONG).show();
    }
    private void showDashboard() {
        root=column();
        TextView brand=text("Lixzzy V1",25,RED,true); brand.setGravity(Gravity.CENTER);
        root.addView(brand);
        TextView status=text("LIXXYZ-V1",11,0xffb8aeb2,true); status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2); sp.bottomMargin=dp(16); root.addView(status,sp);
        TextView section=text("Lixxzy Mods",13,Color.WHITE,true); root.addView(section);
        addFeature(root,"DRAG HS","40%");
        addFeature(root,"HEADLOCK","30%");
        addFeature(root,"AIM PERFECT","10%");
        TextView games=text("GAME LAUNCHER",13,Color.WHITE,true);
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,-2); gp.topMargin=dp(25); root.addView(games,gp);
        TextView ff=button("FREE FIRE");
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(54)); fp.topMargin=dp(12); root.addView(ff,fp);
        ff.setOnClickListener(v->launchGame("com.dts.freefireth"));
        TextView max=button("FREE FIRE MAX");
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(54)); mp.topMargin=dp(10); root.addView(max,mp);
        max.setOnClickListener(v->launchGame("com.dts.freefiremax"));
        TextView disclaimer=text("Lixxzy 2026©",10,0xff9d8f94,false);
        disclaimer.setGravity(Gravity.CENTER); disclaimer.setPadding(0,dp(20),0,dp(12)); root.addView(disclaimer);
        TextView logout=text("LOG OUT",12,RED,true); logout.setGravity(Gravity.CENTER); logout.setPadding(0,dp(12),0,dp(8));
        root.addView(logout); logout.setOnClickListener(v->showLogin());
        base(root);
    }
                                      }
