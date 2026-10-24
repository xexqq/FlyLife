package com.flylife.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.content.SharedPreferences;
import android.os.SystemClock;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) { super.onCreate(state); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); setContentView(new FlyLifeView()); }

    class FlyLifeView extends View {
        Paint p = new Paint(3); Random rng = new Random(); SharedPreferences prefs;
        float fx=0.5f, fy=0.53f, tx=0.7f, ty=0.4f; int mode=0, coins, seed; long last=0, lastReward=0; float phase=0;
        String[] modes={"SIMPLE AI","OBSERVER","TRAINING","FLY LAB"};
        FlyLifeView(){ super(MainActivity.this); prefs=getSharedPreferences("flylife",MODE_PRIVATE); coins=prefs.getInt("coins",0); seed=prefs.getInt("seed",(int)System.currentTimeMillis()); fx=prefs.getFloat("fx",0.5f); fy=prefs.getFloat("fy",0.5f); lastReward=prefs.getLong("last",System.currentTimeMillis()); long hours=Math.max(0,Math.min(24,(System.currentTimeMillis()-lastReward)/3600000)); coins+=(int)hours; prefs.edit().putInt("coins",coins).putLong("last",System.currentTimeMillis()).apply(); }
        void color(Canvas c,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);}
        void text(Canvas c,String s,float x,float y,float size,int col){color(c,col);p.setTextSize(size);c.drawText(s,x,y,p);}
        @Override protected void onDraw(Canvas c){super.onDraw(c); float w=getWidth(),h=getHeight(); c.drawColor(Color.rgb(20,27,39));
            // Room and procedural furniture layout derived from seed
            float left=w*.055f, top=h*.12f, right=w*.79f, bottom=h*.88f;
            color(c,0xffd8c5a3); c.drawRoundRect(new RectF(left,top,right,bottom),18,18,p);
            color(c,0xff8d7258); c.drawRect(left,top,right,top+12,p); c.drawRect(left,bottom-12,right,bottom,p); c.drawRect(left,top,left+12,bottom,p); c.drawRect(right-12,top,right,bottom,p);
            // window
            color(c,0xff79c7e3); c.drawRoundRect(new RectF(w*.33f,top+4,w*.49f,top+15),3,3,p); color(c,0xffe9f8ff); c.drawRect(w*.405f,top+4,w*.411f,top+15,p);
            // seed-generated furniture
            rng.setSeed(seed); int layout=rng.nextInt(3);
            color(c,0xff527c8e); c.drawRoundRect(new RectF(w*(layout==0?.12f:.58f),h*.55f,w*(layout==0?.27f:.72f),h*.76f),12,12,p);
            color(c,0xfff0d9ad); c.drawRoundRect(new RectF(w*.18f,h*.58f,w*.25f,h*.65f),7,7,p);
            color(c,0xff6d8f57); c.drawRect(w*.59f,h*.23f,w*.62f,h*.37f,p); color(c,0xff4d7142); c.drawCircle(w*.605f,h*.22f,Math.min(w,h)*.045f,p);
            color(c,0xff9e6b45); c.drawRoundRect(new RectF(w*.68f,h*.68f,w*.75f,h*.77f),8,8,p); color(c,0xffffdf76); c.drawCircle(w*.715f,h*.665f,Math.min(w,h)*.018f,p);
            // food marker
            color(c,0xffdc6b53); c.drawCircle(w*.39f,h*.72f,Math.min(w,h)*.014f,p); text(c,"FOOD",w*.37f,h*.77f,Math.min(w,h)*.018f,0xff68432f);
            // fly movement
            long now=SystemClock.uptimeMillis(); float dt=last==0?.016f:Math.min(.05f,(now-last)/1000f); last=now; phase+=dt;
            if(mode!=2){ float dx=tx-fx,dy=ty-fy,dist=(float)Math.sqrt(dx*dx+dy*dy); if(dist<.018f || (rng.nextFloat()<.008f)){tx=.12f+rng.nextFloat()*.62f;ty=.2f+rng.nextFloat()*.6f;} else {fx+=dx/dist*dt*(mode==1?.12f:.2f);fy+=dy/dist*dt*(mode==1?.10f:.17f);} }
            float bx=w*(left/w+fx*(right-left)/w), by=h*(top/h+fy*(bottom-top)/h); float r=Math.min(w,h)*.022f;
            color(c,0xff151515); c.drawOval(new RectF(bx-r*1.2f,by-r*.45f,bx+r*1.2f,by+r*.45f),p); color(c,0xff4b4141); c.drawOval(new RectF(bx-r*.45f,by-r*.7f,bx+r*.45f,by+r*.7f),p);
            color(c,0xbbe8f8ff); c.drawOval(new RectF(bx-r*1.05f,by-r*1.3f,bx-r*.1f,by-r*.35f),p); c.drawOval(new RectF(bx+r*.05f,by-r*1.3f,bx+r*1.0f,by-r*.35f),p);
            // HUD
            color(c,0xff111827); c.drawRoundRect(new RectF(12,12,w*.29f,h*.105f),16,16,p); text(c,"FLYLIFE  •  PROTOTYPE 0.1",26,38,Math.min(w,h)*.024f,Color.WHITE); text(c,"Coins: "+coins+"   Seed: "+seed,26,62,Math.min(w,h)*.021f,0xffc9d5e7);
            float bx0=w*.81f; text(c,"MODES",bx0,h*.16f,Math.min(w,h)*.026f,0xffc9d5e7);
            for(int i=0;i<modes.length;i++){float y=h*(.21f+i*.115f);color(c,mode==i?0xff397b78:0xff293548);c.drawRoundRect(new RectF(bx0,y,w*.97f,y+h*.083f),12,12,p);text(c,modes[i],bx0+14,y+h*.052f,Math.min(w,h)*.024f,Color.WHITE);}
            color(c,0xff293548);c.drawRoundRect(new RectF(bx0,h*.70f,w*.97f,h*.79f),12,12,p);text(c,"NEW HOUSE",bx0+14,h*.753f,Math.min(w,h)*.024f,Color.WHITE);
            text(c,mode==0?"Fly explores automatically":mode==1?"Relax and observe":mode==2?"Tap room to set a target":"Lab: observe behaviour",bx0,h*.85f,Math.min(w,h)*.018f,0xffc9d5e7);
            if(mode==2){color(c,0xfff3c969);c.drawCircle(tx*w,ty*h,Math.min(w,h)*.012f,p);} postInvalidateDelayed(16);
        }
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY(),w=getWidth(),h=getHeight(),bx=w*.81f;
            if(x>bx){int idx=(int)((y/h-.21f)/.115f);if(y>h*.70f&&y<h*.80f){seed=rng.nextInt(999999);rng.setSeed(seed);fx=.5f;fy=.5f;tx=.7f;ty=.4f;}else if(idx>=0&&idx<4&&y<h*.21f+4*.115f*h){mode=idx;}else if(y<h*.21f){mode=0;}}
            else if(mode==2){tx=Math.max(.05f,Math.min(.95f,(x-w*.055f)/(w*.79f-w*.055f)));ty=Math.max(.05f,Math.min(.95f,(y-h*.12f)/(h*.88f-h*.12f)));}else {coins++;prefs.edit().putInt("coins",coins).apply();}
            prefs.edit().putInt("seed",seed).putInt("mode",mode).putFloat("fx",fx).putFloat("fy",fy).putLong("last",System.currentTimeMillis()).apply();invalidate();return true;}
    }
}
