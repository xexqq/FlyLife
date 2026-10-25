package com.flylife.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Random;

/** FlyLife 3D: native OpenGL ES 2.0 procedural house sandbox. */
public class MainActivity extends Activity {
    private FlyWorld3D world;
    private TextView status;
    private final String[] modes={"Свободная жизнь","Побег","Обучение","Fly Lab","Эволюция","Real Brain"};
    private int mode=0;
    private long seed=0x46f19a31L;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);prefs=getSharedPreferences("flylife_3d",MODE_PRIVATE);seed=prefs.getLong("seed",seed);mode=prefs.getInt("mode",0);buildUi();}
    private void buildUi(){
        FrameLayout root=new FrameLayout(this);
        GLSurfaceView gl=new GLSurfaceView(this);gl.setEGLContextClientVersion(2);world=new FlyWorld3D(this,seed);world.setMode(mode);gl.setRenderer(world);gl.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);gl.setOnTouchListener((v,e)->world.handleTouch(e));root.addView(gl,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(8),dp(5),dp(8),dp(5));top.setBackgroundColor(0xAA101925);
        Button modeBtn=button("РЕЖИМ: "+modes[mode]);modeBtn.setOnClickListener(v->{mode=(mode+1)%modes.length;world.setMode(mode);modeBtn.setText("РЕЖИМ: "+modes[mode]);save();});top.addView(modeBtn,new LinearLayout.LayoutParams(0,dp(42),1));
        Button seedBtn=button("SEED");seedBtn.setOnClickListener(v->{EditText input=new EditText(this);input.setSingleLine(true);input.setInputType(2|0x1000);input.setText(Long.toString(seed));new AlertDialog.Builder(this).setTitle("Seed мира").setMessage("Одинаковый seed создаёт тот же дом").setView(input).setNegativeButton("Отмена",null).setPositiveButton("Создать",(d,w)->{try{seed=Long.parseLong(input.getText().toString().trim());world.newWorld(seed);save();}catch(Exception ex){status.setText("Seed должен быть целым числом");}}).show();});top.addView(seedBtn);
        Button reset=button("↺");reset.setOnClickListener(v->world.resetCamera());top.addView(reset);FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(52),Gravity.TOP);root.addView(top,tp);
        LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER);bottom.setPadding(dp(6),dp(4),dp(6),dp(4));bottom.setBackgroundColor(0xAA101925);
        Button food=button("Еда");food.setOnClickListener(v->world.spawnFood());bottom.addView(food,new LinearLayout.LayoutParams(0,dp(42),1));
        Button pause=button("Пауза");pause.setOnClickListener(v->{world.togglePause();pause.setText(world.isPaused()?"Продолжить":"Пауза");});bottom.addView(pause,new LinearLayout.LayoutParams(0,dp(42),1));
        Button help=button("Управление");help.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("FlyLife 3D").setMessage("ПЕРЕТАСКИВАНИЕ одним пальцем — вращать камеру.\nЩИПОК двумя пальцами — приблизить/отдалить.\nДВА пальца — сдвинуть камеру.\nКАСАНИЕ пола — задать цель в режимах Обучение и Fly Lab.\nКнопка SEED создаёт воспроизводимый дом.\nМуха летает с инерцией и выбирает исследовательские точки, а не идеальную прямую.").setPositiveButton("Понятно",null).show());bottom.addView(help,new LinearLayout.LayoutParams(0,dp(42),1));
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(52),Gravity.BOTTOM);root.addView(bottom,bp);
        status=new TextView(this);status.setTextColor(Color.WHITE);status.setTextSize(12);status.setPadding(dp(10),dp(5),dp(10),dp(5));status.setBackgroundColor(0x99202E3F);FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.RIGHT);sp.topMargin=dp(58);sp.rightMargin=dp(8);root.addView(status,sp);world.setStatusListener(s->runOnUiThread(()->status.setText(s)));
        setContentView(root);
    }
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff2b4054));return b;}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private void save(){prefs.edit().putLong("seed",seed).putInt("mode",mode).apply();}
    @Override protected void onPause(){if(world!=null)world.persist();save();super.onPause();}
    @Override protected void onResume(){super.onResume();if(world!=null)world.resume();}
    @Override public void onBackPressed(){new AlertDialog.Builder(this).setTitle("Выйти из FlyLife?").setMessage("Прогресс будет сохранён.").setNegativeButton("Остаться",null).setPositiveButton("Выйти",(d,w)->{if(world!=null)world.persist();finish();}).show();}
}
