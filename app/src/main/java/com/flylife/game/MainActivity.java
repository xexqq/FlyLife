package com.flylife.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.text.InputType;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** FlyLife 1.0 — a standalone, seed-driven 2D fly-life sandbox. */
public class MainActivity extends Activity {
    private Game game;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        game = new Game();
        setContentView(game);
    }
    @Override protected void onPause() { if (game != null) game.persist(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); if (game != null) game.applyOfflineIncome(); }
    @Override public void onBackPressed() {
        if (game != null && game.handleSystemBack()) return;
        super.onBackPressed();
    }

    static final int MODE_ESCAPE=0, MODE_FREE=1, MODE_TRAIN=2, MODE_LAB=3, MODE_EVOLUTION=4, MODE_BRAIN=5;
    static final String[] MODE_NAMES={"ПОБЕГ", "СВОБОДНАЯ ЖИЗНЬ", "ОБУЧЕНИЕ", "FLY LAB", "ЭВОЛЮЦИЯ", "REAL BRAIN"};
    static final String[] MODE_DESC={
            "Найди выход из дома. Муха сама исследует комнаты, ищет еду и прокладывает путь к двери.",
            "Наблюдай за повседневной жизнью: поиск еды и света у окон, отдых и формирование памяти.",
            "Коснись пола, чтобы задать цель. Муха должна найти маршрут и добраться до точки.",
            "Экспериментальный ручной режим. Касайся доступных мест и наблюдай, как агент обходит мебель.",
            "Муха меняется с опытом: развивается любопытство, скорость и исследовательское поведение.",
            "Демонстрация сенсорных и моторных контуров. Это упрощённая модель для экспериментов, не реальный FlyWire-коннектом."
    };
    static final String[] HOUSE_NAMES={"СЕМЕЙНЫЙ ДОМ","БОЛЬШОЙ ДОМ","УСАДЬБА","ОСОБНЯК"};
    static final String[] TRAIT_NAMES={"Любопытная","Осторожная","Смелая","Непоседа","Спокойная","Нюхач"};
    static final String[] FLY_NAMES={"Пип","Искра","Комета","Шустрик","Капля","Мидж"};
    static final int[] SHELF_COLORS={0xffa7c0bf,0xffd4a071,0xffc1c1d9,0xff87a77e};
    static final int[] WORKBENCH_COLORS={0xff6f8997,0xffb56d4e,0xffd1b56c,0xff82a579};
    static final String[] ACH_NAMES={"Первый полёт","Первая еда","Большой побег","Друг в пасеке","Первые уроки","Хозяин пасеки","Исследователь","Картограф","Гурман","Ветеран","Эволюция","Коллекционер кожи","Новый мир"};
    static final String[] ACH_DESC={"Муха начала исследовать мир.","Найди первую порцию еды.","Впервые выберись из дома.","Сохрани первую муху.","Заверши учебную цель.","Сохрани трёх мух.","Посети четыре комнаты.","Изучи все комнаты этого дома.","Съешь десять порций еды.","Достигни пятого уровня.","Достигни третьего уровня эволюции.","Открой три скина.","Исследуй три разных мира."};
    static final Skin[] SKINS={
            new Skin("Домовая",0xff41352b,0xff241f1d,0xffd3e7e8,0xff9f7551,0),
            new Skin("Янтарь",0xffb97827,0xff5d3517,0xffffedb1,0xfff2b64d,45),
            new Skin("Лесная",0xff386d45,0xff183d2c,0xffc7f0d2,0xff7dc88a,70),
            new Skin("Синяя",0xff306e9b,0xff173752,0xffd9f6ff,0xff67c7ed,90),
            new Skin("Розовая",0xffc75e86,0xff6d2544,0xffffe0ef,0xffff96bf,110),
            new Skin("Фиолетовая",0xff7951a8,0xff392454,0xfff0e2ff,0xffb994fb,130),
            new Skin("Ледяная",0xff95c9d8,0xff365b72,0xfff1ffff,0xffb6f7ff,150),
            new Skin("Золотая",0xffd7aa34,0xff73521c,0xfffff2bd,0xffffda69,180),
            new Skin("Неоновая",0xff78b82b,0xff314a16,0xffe7ffad,0xffd4ff47,200),
            new Skin("Алая",0xffbd3e3e,0xff5c2024,0xffffd6cb,0xffff7b68,230),
            new Skin("Космос",0xff514d8d,0xff25203f,0xffe9e4ff,0xff9b91ff,260),
            new Skin("Призма",0xff30a9a6,0xff255b69,0xffe6ffff,0xff7ff5e6,300)
    };
    static final class Skin {
        final String name; final int body, thorax, wing, accent, cost;
        Skin(String n,int b,int t,int w,int a,int c){name=n;body=b;thorax=t;wing=w;accent=a;cost=c;}
    }
    static final class FlyProfile {
        String name="Пип"; int skin=0, trait=0, level=1, xp=0, meals=0, escapes=0, visitedMask=0, worldSeedHash=0;
        float curiosity=.68f, caution=.40f, speedGene=.52f, stamina=1f;
        long born=System.currentTimeMillis();
        FlyProfile copy(){FlyProfile f=new FlyProfile();f.name=name;f.skin=skin;f.trait=trait;f.level=level;f.xp=xp;f.meals=meals;f.escapes=escapes;f.visitedMask=visitedMask;f.worldSeedHash=worldSeedHash;f.curiosity=curiosity;f.caution=caution;f.speedGene=speedGene;f.stamina=stamina;f.born=born;return f;}
    }

    final class Game extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        private final RectF rectTmp=new RectF();
        private final SharedPreferences prefs=getSharedPreferences("flylife_release",MODE_PRIVATE);
        private final Random random=new Random();
        private WorldModel world;
        private FlyProfile fly=new FlyProfile();
        private final List<FlyProfile> apiary=new ArrayList<>();
        private long seed, frameTime, lastSave, lastDecision, toastAt, lastApiaryTick, lastEvolution;
        private int mode=MODE_FREE, screen=0, selectedFly=-1, coins=0, achievementMask=0, skinMask=1,
                worldsSeen=1, trainingTasks=0, escapes=0, totalMeals=0, cosmeticSelected=0;
        private int runVisited=0;
        private float fx=.16f, fy=.18f, vx=0, vy=0, tx=.5f, ty=.5f, hunger=.18f, energy=.92f;
        private float targetMarkerX=.5f, targetMarkerY=.5f;
        private boolean targetActive=false, paused=false, escaped=false, showHelp=false, showBrainPanel=false, dirty=false;
        private String toast="FlyLife 1.0 готова к исследованию", flyState="Исследует дом";
        private int brainPulse=0; private long lastBrainPulse=0;
        private float W,H, mapL,mapT,mapR,mapB,cell,houseW,houseH;
        private float wingPhase=0, elapsed=0;
        private boolean routeCached=false; private int navStartCell=-1,navGoalCell=-1; private long navTargetSignature=Long.MIN_VALUE; private float navWaypointX=.5f,navWaypointY=.5f; private final float[] navPoint={.5f,.5f};
        private float lastFoodX=-1,lastFoodY=-1;
        private int lastRoom=-1;
        private final int bg=0xff101722, panel=0xff192433, panel2=0xff243346, lineColor=0xff3d5065,
                textColor=0xffecf3fb, muted=0xff9aadc2, mint=0xff70d5b2, gold=0xffffce72, red=0xffff887d;

        Game(){super(MainActivity.this);load();setFocusable(true);}

        private void load(){
            seed=prefs.getLong("seed",0x46f19a31L); coins=Math.max(0,prefs.getInt("coins",0));
            boolean brandNewFly=!prefs.contains("fly_trait");
            mode=clampInt(prefs.getInt("mode",MODE_FREE),0,MODE_BRAIN);showBrainPanel=mode==MODE_BRAIN; achievementMask=prefs.getInt("achievements",0);
            skinMask=prefs.getInt("skins",1)|1; worldsSeen=Math.max(1,prefs.getInt("worldsSeen",1));
            trainingTasks=prefs.getInt("trainingTasks",0);escapes=prefs.getInt("escapes",0);totalMeals=prefs.getInt("meals",0);
            fly.name=prefs.getString("fly_name","Пип");fly.skin=clampInt(prefs.getInt("fly_skin",0),0,SKINS.length-1);fly.trait=clampInt(prefs.getInt("fly_trait",0),0,5);
            fly.level=Math.max(1,prefs.getInt("fly_level",1));fly.xp=Math.max(0,prefs.getInt("fly_xp",0));fly.meals=prefs.getInt("fly_meals",0);fly.escapes=prefs.getInt("fly_escapes",0);fly.visitedMask=prefs.getInt("fly_memory",0);
            fly.curiosity=clamp(prefs.getFloat("fly_curiosity",.68f),.1f,1f);fly.caution=clamp(prefs.getFloat("fly_caution",.4f),.1f,1f);fly.speedGene=clamp(prefs.getFloat("fly_speed",.52f),.1f,1f);fly.stamina=clamp(prefs.getFloat("fly_stamina",1f),.05f,1f);fly.born=prefs.getLong("fly_born",System.currentTimeMillis());
            if(brandNewFly){Random phenotype=new Random(seed^0x3C6EF372FE94F82AL);fly.trait=phenotype.nextInt(FLY_NAMES.length);fly.name=FLY_NAMES[phenotype.nextInt(FLY_NAMES.length)];fly.curiosity=.34f+phenotype.nextFloat()*.62f;fly.caution=.18f+phenotype.nextFloat()*.68f;fly.speedGene=.28f+phenotype.nextFloat()*.68f;}
            hunger=clamp(prefs.getFloat("hunger",.18f),0,1);energy=clamp(prefs.getFloat("energy",.92f),0,1);
            loadApiary();
            world=WorldModel.generate(seed);int savedHash=(int)(seed^(seed>>>32));fly.worldSeedHash=savedHash;
            if(prefs.contains("fx")){float savedX=clamp(prefs.getFloat("fx",.16f),.03f,.97f),savedY=clamp(prefs.getFloat("fy",.18f),.03f,1.02f);if(savedY>=1f||world.blocked(savedX,savedY,.03f)){spawnAtStart();}else{float[] safe=world.resolveTarget(savedX,savedY);fx=safe[0];fy=safe[1];}}else spawnAtStart();
            runVisited=prefs.getInt("runVisited",0);fly.visitedMask=runVisited;tx=prefs.getFloat("tx",.5f);ty=prefs.getFloat("ty",.5f);
            lastApiaryTick=SystemClock.uptimeMillis();checkAchievements();
        }
        /** Credits a capped, wall-clock-based apiary bonus when returning from the background. */
        void applyOfflineIncome(){
            if(world==null)return;
            long now=System.currentTimeMillis();
            long last=prefs.getLong("lastWall",now);
            long elapsed=Math.max(0,now-last);
            long minutes=Math.min(7L*24L*60L,elapsed/60000L);
            int rate=apiaryIncomePerMinute();
            long earned=Math.min(1500L,minutes*rate);
            if(earned>0){coins=(int)Math.min(Integer.MAX_VALUE,(long)coins+earned);notifyUser("Пасека заработала офлайн: +"+earned+" монет");}
            lastApiaryTick=SystemClock.uptimeMillis();
            // Persist the new checkpoint even when the payout rounded down to zero.
            persist();
            invalidate();
        }
        private int clampInt(int v,int min,int max){return Math.max(min,Math.min(max,v));}
        private float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        private void spawnAtStart(){int idx=0;for(WorldModel.Room r:world.rooms)if(r.type==WorldModel.HALL){idx=r.index(world.cols);break;}float[] spot=world.randomOpenSpot(idx%world.cols,idx/world.cols,.40f,.60f);fx=spot[0];fy=spot[1];vx=vy=0;routeCached=false;}
        private void loadApiary(){apiary.clear();int count=Math.min(12,Math.max(0,prefs.getInt("apiaryCount",0)));for(int i=0;i<count;i++){String q="apiary_"+i+"_";FlyProfile f=new FlyProfile();f.name=prefs.getString(q+"name","Муха "+(i+1));f.skin=clampInt(prefs.getInt(q+"skin",0),0,SKINS.length-1);f.trait=clampInt(prefs.getInt(q+"trait",0),0,5);f.level=Math.max(1,prefs.getInt(q+"level",1));f.xp=Math.max(0,prefs.getInt(q+"xp",0));f.meals=prefs.getInt(q+"meals",0);f.escapes=prefs.getInt(q+"escapes",0);f.visitedMask=prefs.getInt(q+"memory",0);f.curiosity=clamp(prefs.getFloat(q+"curiosity",.68f),.1f,1f);f.caution=clamp(prefs.getFloat(q+"caution",.4f),.1f,1f);f.speedGene=clamp(prefs.getFloat(q+"speed",.52f),.1f,1f);f.stamina=clamp(prefs.getFloat(q+"stamina",1f),.05f,1f);f.born=prefs.getLong(q+"born",System.currentTimeMillis());apiary.add(f);}}
        void persist(){if(world==null)return;SharedPreferences.Editor e=prefs.edit();e.putLong("seed",seed).putInt("coins",coins).putInt("mode",mode).putInt("achievements",achievementMask).putInt("skins",skinMask).putInt("worldsSeen",worldsSeen).putInt("trainingTasks",trainingTasks).putInt("escapes",escapes).putInt("meals",totalMeals).putString("fly_name",fly.name).putInt("fly_skin",fly.skin).putInt("fly_trait",fly.trait).putInt("fly_level",fly.level).putInt("fly_xp",fly.xp).putInt("fly_meals",fly.meals).putInt("fly_escapes",fly.escapes).putInt("fly_memory",runVisited).putFloat("fly_curiosity",fly.curiosity).putFloat("fly_caution",fly.caution).putFloat("fly_speed",fly.speedGene).putFloat("fly_stamina",fly.stamina).putLong("fly_born",fly.born).putFloat("fx",fx).putFloat("fy",fy).putFloat("hunger",hunger).putFloat("energy",energy).putFloat("tx",tx).putFloat("ty",ty).putInt("runVisited",runVisited).putLong("lastWall",System.currentTimeMillis());
            e.putInt("apiaryCount",apiary.size());for(int i=0;i<apiary.size();i++){FlyProfile f=apiary.get(i);String q="apiary_"+i+"_";e.putString(q+"name",f.name).putInt(q+"skin",f.skin).putInt(q+"trait",f.trait).putInt(q+"level",f.level).putInt(q+"xp",f.xp).putInt(q+"meals",f.meals).putInt(q+"escapes",f.escapes).putInt(q+"memory",f.visitedMask).putFloat(q+"curiosity",f.curiosity).putFloat(q+"caution",f.caution).putFloat(q+"speed",f.speedGene).putFloat(q+"stamina",f.stamina).putLong(q+"born",f.born);}e.apply();dirty=false;lastSave=SystemClock.uptimeMillis();}
        private void notifyUser(String message){toast=message;toastAt=SystemClock.uptimeMillis();dirty=true;}
        private void reward(int amount,String message){coins=Math.max(0,coins+amount);notifyUser(message);}
        private void unlock(int bit){if((achievementMask&(1<<bit))==0){achievementMask|=1<<bit;coins+=15;notifyUser("Достижение: "+ACH_NAMES[bit]+" · +15 монет");dirty=true;}}
        private void checkAchievements(){if(totalMeals>0)unlock(1);if(escapes>0)unlock(2);if(apiary.size()>0)unlock(3);if(trainingTasks>0)unlock(4);if(apiary.size()>=3)unlock(5);if(Integer.bitCount(runVisited)>=4)unlock(6);if(world!=null&&Integer.bitCount(runVisited)>=world.rooms.length)unlock(7);if(totalMeals>=10)unlock(8);if(fly.level>=5)unlock(9);if(fly.level>=3&&mode==MODE_EVOLUTION)unlock(10);if(Integer.bitCount(skinMask)>=3)unlock(11);if(worldsSeen>=3)unlock(12);}
        private void gainXp(int n){int old=fly.level;fly.xp+=n;fly.level=1+fly.xp/35;if(fly.level>old){notifyUser("Новый уровень мухи: "+fly.level+"!");if(mode==MODE_EVOLUTION&&SystemClock.uptimeMillis()-lastEvolution>2000){lastEvolution=SystemClock.uptimeMillis();fly.curiosity=clamp(fly.curiosity+(random.nextFloat()-.35f)*.08f,.18f,1f);fly.speedGene=clamp(fly.speedGene+(random.nextFloat()-.45f)*.05f,.25f,1f);fly.caution=clamp(fly.caution+(random.nextFloat()-.5f)*.04f,.15f,.9f);}}dirty=true;checkAchievements();}

        private void newWorld(long nextSeed){boolean changed=seed!=nextSeed;seed=nextSeed;world=WorldModel.generate(seed);spawnAtStart();invalidateRoute();hunger=Math.max(.08f,hunger);energy=Math.max(.4f,energy);runVisited=0;fly.visitedMask=0;fly.worldSeedHash=(int)(seed^(seed>>>32));escaped=false;targetActive=false;paused=false;lastRoom=-1;lastDecision=0;if(changed)worldsSeen++;reward(0,"Новый мир с seed "+seed);checkAchievements();persist();}
        private void chooseFoodTarget(){WorldModel.Food best=null;float bd=Float.MAX_VALUE;for(WorldModel.Food f:world.food)if(f.active){float d=dist(fx,fy,f.x,f.y);if(d<bd){bd=d;best=f;}}if(best!=null){tx=best.x;ty=best.y;flyState="Чует запах еды";}else chooseExploreTarget();}
        private float dist(float x1,float y1,float x2,float y2){return (float)Math.hypot(x1-x2,y1-y2);}
        private void chooseExploreTarget(){
            int current=world.roomAt(fx,fy), best=-1;float bestScore=-999f;
            for(WorldModel.Room r:world.rooms){int idx=r.index(world.cols);float score=0;boolean visited=(runVisited&(1<<idx))!=0;score += visited?-1.0f:3.4f;score-=Math.abs(r.x-current%world.cols)*.52f+Math.abs(r.y-current/world.cols)*.52f;score+=random.nextFloat()*fly.curiosity*.9f;if(r.type==WorldModel.KITCHEN&&hunger>.28f)score+=.6f;if(r.type==WorldModel.BEDROOM&&energy<.25f)score+=.9f;if(score>bestScore){bestScore=score;best=idx;}}
            if(best<0)best=random.nextInt(world.rooms.length);WorldModel.Room r=world.rooms[best];float[] spot=world.randomOpenSpot(r.x,r.y,.30f,.75f);tx=spot[0];ty=spot[1];flyState=(runVisited&(1<<best))==0?"Изучает новую комнату":"Исследует дом";
        }
        private void chooseRestTarget(){int idx=0;for(WorldModel.Room r:world.rooms)if(r.type==WorldModel.BEDROOM){idx=r.index(world.cols);break;}WorldModel.Room r=world.rooms[idx];float[] spot=world.randomOpenSpot(r.x,r.y,.55f,.85f);tx=spot[0];ty=spot[1];flyState="Отдыхает";}
        /** Finds an actual window-lit room and chooses a safe grid cell near its light pool. */
        private void chooseLightTarget(){
            int current=world.roomAt(fx,fy),best=-1;float bestScore=-999f;
            for(WorldModel.Room r:world.rooms){boolean topWindow=r.y==0&&(r.x%2)==0,rightWindow=r.x==world.cols-1&&(r.y%2)==0;if(!topWindow&&!rightWindow)continue;
                int idx=r.index(world.cols);float score=((runVisited&(1<<idx))==0?1.0f:-.25f)-(.42f*(Math.abs(r.x-current%world.cols)+Math.abs(r.y-current/world.cols)))+random.nextFloat()*.45f;
                if(score>bestScore){bestScore=score;best=idx;}}
            if(best<0){chooseExploreTarget();return;}WorldModel.Room room=world.rooms[best];float lx,ly;
            if(room.y==0&&(room.x%2)==0){lx=(room.x+.5f)/world.cols;ly=(room.y+.12f)/world.rows;}
            else{lx=(room.x+.88f)/world.cols;ly=(room.y+.5f)/world.rows;}
            float[] goal=world.resolveTarget(lx,ly);tx=goal[0];ty=goal[1];flyState="Летит к свету у окна";
        }
        private void updateAutoTarget(long now){
            if(mode==MODE_ESCAPE){tx=.5f;ty=1.025f;flyState="Ищет выход";return;}
            if(mode==MODE_TRAIN){if(!targetActive)flyState="Ждёт учебную цель";return;}
            if(mode==MODE_LAB){if(targetActive)flyState="Идёт к цели";else flyState="Fly Lab: жди команды";return;}
            if(mode==MODE_BRAIN){
                if(now-lastBrainPulse>240){brainPulse=(brainPulse+1)%4;lastBrainPulse=now;}
                float odor=0;for(WorldModel.Food f:world.food)if(f.active){float d=dist(fx,fy,f.x,f.y);odor=Math.max(odor,clamp(.7f-d*2.5f,0,1));}
                float foodDrive=hunger*.72f+odor*.58f;float exploreDrive=fly.curiosity*.50f;float restDrive=(1f-energy)*.62f;
                if(now-lastDecision>900||dist(fx,fy,tx,ty)<.025f){if(foodDrive>exploreDrive&&foodDrive>restDrive&&hunger>.28f)chooseFoodTarget();else if(restDrive>.70f&&energy<.24f)chooseRestTarget();else if(fly.curiosity>.62f&&energy>.4f&&random.nextFloat()<.30f)chooseLightTarget();else chooseExploreTarget();lastDecision=now;}
                return;
            }
            boolean targetReached=dist(fx,fy,tx,ty)<.024f;
            if(hunger>.48f && (now-lastDecision>1400 || targetReached)){chooseFoodTarget();lastDecision=now;return;}
            if(energy<.17f && targetReached){chooseRestTarget();lastDecision=now;return;}
            if(now-lastDecision>1800 || targetReached || (hunger>.35f&&foodNearby())){if(hunger>.34f&&foodNearby())chooseFoodTarget();else if(energy<.14f)chooseRestTarget();else if(fly.curiosity>.58f&&energy>.38f&&random.nextFloat()<.24f)chooseLightTarget();else chooseExploreTarget();lastDecision=now;}
        }
        private boolean foodNearby(){for(WorldModel.Food f:world.food)if(f.active&&dist(fx,fy,f.x,f.y)<.24f)return true;return false;}
        private void invalidateRoute(){routeCached=false;}
        private float[] cachedWaypoint(){
            int gridW=world.cols*WorldModel.GRID,gridH=world.rows*WorldModel.GRID;
            int sx=Math.max(0,Math.min(gridW-1,(int)(fx*gridW)));
            int sy=Math.max(0,Math.min(gridH,(int)(fy*gridH)));
            int gx=Math.max(0,Math.min(gridW-1,(int)(tx*gridW)));
            int gy=Math.max(0,Math.min(gridH,(int)(ty*gridH)));
            long sig=((long)Float.floatToIntBits(tx)<<32)^(Float.floatToIntBits(ty)&0xffffffffL);
            int sc=sy*gridW+sx,gc=gy*gridW+gx;
            if(!routeCached||sc!=navStartCell||gc!=navGoalCell||sig!=navTargetSignature){float[] wp=world.nextWaypoint(fx,fy,tx,ty);navWaypointX=wp[0];navWaypointY=wp[1];navStartCell=sc;navGoalCell=gc;navTargetSignature=sig;routeCached=true;}
            navPoint[0]=navWaypointX;navPoint[1]=navWaypointY;return navPoint;
        }
        private void updateWorld(float dt,long now){
            if(screen!=0||paused||escaped)return;
            elapsed+=dt;wingPhase+=dt*17f;hunger=Math.min(1f,hunger+dt*.0022f);energy=Math.max(.08f,energy-dt*.00062f);
            if(energy<.995f&&Math.hypot(vx,vy)<.015f){energy=Math.min(1f,energy+dt*.035f);if(energy>.7f&&"Отдыхает".equals(flyState))flyState="Исследует дом";}
            for(WorldModel.Food f:world.food)if(!f.active&&now>=f.respawnAt){f.active=true;}
            int room=world.roomAt(fx,fy);if(room!=lastRoom){int bit=1<<room;if((runVisited&bit)==0){runVisited|=bit;fly.visitedMask=runVisited;gainXp(2);coins+=1;notifyUser("Новая комната: "+WorldModel.ROOM_NAMES[world.rooms[room].type]+" · +1 монета");if(Integer.bitCount(runVisited)>=4)unlock(6);if(Integer.bitCount(runVisited)>=world.rooms.length)unlock(7);}lastRoom=room;dirty=true;}
            updateAutoTarget(now);
            float[] wp=cachedWaypoint();float dx=wp[0]-fx,dy=wp[1]-fy,d=dist(fx,fy,wp[0],wp[1]);
            float traitSpeed=fly.trait==2?.035f:(fly.trait==3?.045f:(fly.trait==1?-.025f:(fly.trait==4?-.035f:0f)));float baseSpeed=.18f+fly.speedGene*.10f+traitSpeed;float speed=baseSpeed*(.78f+energy*.22f)*(1f-fly.caution*.055f);if(mode==MODE_TRAIN)speed*=1.08f;if(mode==MODE_ESCAPE)speed*=1.05f;if(mode==MODE_LAB)speed*=1.2f;if(mode==MODE_BRAIN)speed*=.94f;
            if(d>.004f){float step=Math.min(d,speed*dt);float nx=fx+dx/d*step,ny=fy+dy/d*step;boolean moved=false;
                if(world.canMove(fx,fy,nx,ny)){fx=nx;fy=ny;moved=true;}
                else if(world.canMove(fx,fy,nx,fy)){fx=nx;moved=true;}
                else if(world.canMove(fx,fy,fx,ny)){fy=ny;moved=true;}
                if(!moved){vx=vy=0;lastDecision=0;invalidateRoute();}else{vx=dx/d*speed;vy=dy/d*speed;unlock(0);}
            }else{vx=vy=0;}
            for(WorldModel.Food f:world.food)if(f.active&&dist(fx,fy,f.x,f.y)<.028f){f.active=false;f.respawnAt=now+6500+random.nextInt(6000);hunger=Math.max(0,hunger-.68f);energy=Math.min(1f,energy+.16f);fly.meals++;totalMeals++;gainXp(5);reward(3,"Еда найдена и съедена · +3 монеты");if(totalMeals>=10)unlock(8);dirty=true;break;}
            if(mode==MODE_ESCAPE&&fy>=1.0f&&Math.abs(fx-.5f)<.13f){escaped=true;escapes++;fly.escapes++;gainXp(25);levelToast();reward(35,"ПОБЕГ УДАЛСЯ! +35 монет");unlock(2);persist();}
            if(mode==MODE_TRAIN&&targetActive&&dist(fx,fy,targetMarkerX,targetMarkerY)<.035f){targetActive=false;trainingTasks++;gainXp(8);reward(8,"Цель достигнута · +8 монет");if(trainingTasks>0)unlock(4);persist();}
            if(mode==MODE_LAB&&targetActive&&dist(fx,fy,targetMarkerX,targetMarkerY)<.035f)targetActive=false;
            if(mode==MODE_EVOLUTION&&fly.level>=3)unlock(10);
            if(apiary.size()>=3)unlock(5);
            if(now-lastSave>6000)persist();
        }
        private void levelToast(){if(fly.level>=5)unlock(9);}

        private void accrueApiaryIncome(long now){
            if(lastApiaryTick<=0){lastApiaryTick=now;return;}
            long minutes=Math.max(0,(now-lastApiaryTick)/60000L);
            if(minutes<=0)return;
            int rate=apiaryIncomePerMinute();
            if(rate>0){long earned=Math.min(1_000_000L,minutes*(long)rate);coins=(int)Math.min(Integer.MAX_VALUE,(long)coins+earned);notifyUser("Пасека приносит доход: +"+earned+" монет");persist();}
            lastApiaryTick+=minutes*60000L;
        }
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);W=getWidth();H=getHeight();if(W<=0||H<=0)return;canvas.drawColor(bg);long now=SystemClock.uptimeMillis();float dt=frameTime==0?.016f:Math.min(.045f,Math.max(.001f,(now-frameTime)/1000f));frameTime=now;accrueApiaryIncome(now);
            if(screen==1){drawApiary(canvas);postInvalidateDelayed(100);return;}
            if(screen==2){drawAchievements(canvas);postInvalidateDelayed(100);return;}
            if(screen==3){drawSettings(canvas);postInvalidateDelayed(100);return;}
            if(screen==4){drawGuide(canvas);postInvalidateDelayed(100);return;}
            layoutWorld();updateWorld(dt,now);drawBackdrop(canvas);drawWorld(canvas);drawFood(canvas);drawFly(canvas);drawWorldHeader(canvas);drawPanel(canvas);
            if(showHelp)drawHelpOverlay(canvas);if(mode==MODE_BRAIN&&showBrainPanel)drawBrainOverlay(canvas);if(escaped)drawEscapeOverlay(canvas);
            if(toastAt>0&&now-toastAt<3000)drawToast(canvas,toast);postInvalidateDelayed(16);
        }
        private void layoutWorld(){float areaL=W*.025f,areaR=W*.715f,areaT=H*.145f,areaB=H*.91f;cell=Math.min((areaR-areaL)/world.cols,(areaB-areaT)/world.rows);houseW=cell*world.cols;houseH=cell*world.rows;mapL=areaL+(areaR-areaL-houseW)*.5f;mapT=areaT+(areaB-areaT-houseH)*.5f;mapR=mapL+houseW;mapB=mapT+houseH;}
        private float px(float x){return mapL+x*houseW;} private float py(float y){return mapT+y*houseH;}
        private void bgRect(Canvas c,float l,float t,float r,float b,int color,float radius){p.setStyle(Paint.Style.FILL);p.setColor(color);rectTmp.set(l,t,r,b);c.drawRoundRect(rectTmp,radius,radius,p);}
        private void rect(Canvas c,float l,float t,float r,float b,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRect(l,t,r,b,p);}
        private void circle(Canvas c,float x,float y,float r,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawCircle(x,y,r,p);}
        private void oval(Canvas c,float l,float t,float r,float b,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);rectTmp.set(l,t,r,b);c.drawOval(rectTmp,p);}
        private void strokeRect(Canvas c,float l,float t,float r,float b,int color,float sw,float rad){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(color);rectTmp.set(l,t,r,b);c.drawRoundRect(rectTmp,rad,rad,p);p.setStyle(Paint.Style.FILL);}
        private void line(Canvas c,float x1,float y1,float x2,float y2,int color,float sw){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);}
        private void text(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.LEFT);c.drawText(s,x,y,p);}
        private void centerText(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
        private void fitText(Canvas c,String s,float x,float y,float maxWidth,float size,int color){p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));float w=p.measureText(s);if(w>maxWidth&&w>0)size*=maxWidth/w;text(c,s,x,y,size,color);}
        private void drawBackdrop(Canvas c){
            // Ambient vignette and softly floating dust, drawn procedurally without external assets.
            rect(c,0,0,W,H,0xff111a26);for(int i=0;i<18;i++){long mx=(i*97L+seed*13L)%997L,my=(i*61L+seed*7L)%521L;if(mx<0)mx+=997L;if(my<0)my+=521L;float x=(float)mx/997f*W,y=(float)my/521f*H;circle(c,x,y,1.1f+(i%3),0x243f8090);}
            bgRect(c,W*.016f,H*.025f,W*.716f,H*.965f,0xff151f2d,18);bgRect(c,W*.728f,H*.025f,W*.986f,H*.965f,panel,18);strokeRect(c,W*.016f,H*.025f,W*.716f,H*.965f,0xff29394a,1.2f,18);strokeRect(c,W*.728f,H*.025f,W*.986f,H*.965f,0xff2b3b4f,1.2f,18);
        }
        private int floorColor(int type){switch(type){case WorldModel.KITCHEN:return 0xffc0ae8b;case WorldModel.BEDROOM:return 0xff96795f;case WorldModel.BATHROOM:return 0xffa7c0c5;case WorldModel.HALL:return 0xffa98b6b;case WorldModel.STUDY:return 0xffb39a77;case WorldModel.WORKSHOP:return 0xff858d92;case WorldModel.CONSERVATORY:return 0xff9eb58d;default:return 0xffb99c77;}}
        private void drawWorld(Canvas c){
            // House shadow, dark exterior shell, floorboards and subtle room-specific materials.
            bgRect(c,mapL-9,mapT-9,mapR+9,mapB+9,0x88000000,12);bgRect(c,mapL-6,mapT-6,mapR+6,mapB+6,0xff3f4a53,7);rect(c,mapL,mapT,mapR,mapB,0xff675747);
            for(WorldModel.Room room:world.rooms){float l=mapL+room.x*cell,t=mapT+room.y*cell,r=l+cell,b=t+cell;int fc=floorColor(room.type);rect(c,l+2,t+2,r-2,b-2,fc);
                // Floorboard seams and alternating boards give the flat map a designed, tactile look.
                for(int row=0;row<7;row++){float yy=t+cell*(.08f+row*.125f);line(c,l+4,yy,r-4,yy,(row%2==0?0x27705a43:0x1cffffff),1.2f);float seam=l+cell*(.12f+(row%3)*.27f);line(c,seam,yy- cell*.06f,seam,yy+cell*.045f,0x17624c3c,1f);}
                for(int i=0;i<5;i++)line(c,l+cell*i*.2f,t+3,l+cell*i*.2f,b-3,0x0effffff,.8f);
                // Light pools and soft corners make rooms visually distinguishable.
                oval(c,l+cell*.18f,t+cell*.16f,r-cell*.18f,b-cell*.13f,0x12fff8dc);
                if(room.y==0&&(room.x%2)==0)oval(c,l+cell*.23f,t+cell*.015f,l+cell*.77f,t+cell*.27f,0x1efff2c7);
                if(room.x==world.cols-1&&(room.y%2)==0)oval(c,r-cell*.27f,t+cell*.20f,r-cell*.015f,b-cell*.22f,0x18b9eaff);
                if(cell>70)text(c,WorldModel.ROOM_SHORT[room.type],l+cell*.055f,t+cell*.105f,Math.max(8,cell*.075f),0x93624b39);
            }
            for(WorldModel.Furniture f:world.furniture)drawFurniture(c,f);
            drawWallsAndWindows(c);
            // Exit threshold / welcome mat.
            float ex=px(.5f),ey=mapB;bgRect(c,ex-cell*.13f,ey-cell*.045f,ex+cell*.13f,ey+cell*.035f,0xff6c5140,4);for(int i=0;i<4;i++)line(c,ex-cell*.10f+i*cell*.065f,ey-cell*.03f,ex-cell*.10f+i*cell*.065f,ey+cell*.02f,0xff9b7755,1.3f);
            if(mode==MODE_ESCAPE){bgRect(c,ex-cell*.19f,ey+cell*.04f,ex+cell*.19f,ey+cell*.13f,0xff284c43,5);centerText(c,"ВЫХОД",ex,ey+cell*.105f,Math.max(9,cell*.055f),0xffb3f5d6);}
        }
        private void drawWallsAndWindows(Canvas c){
            float thick=Math.max(3.5f,cell*.035f);int wall=0xff57483d,edge=0xffd5c1a1;
            // Exterior walls deliberately have a door-sized opening at the bottom centre.
            line(c,mapL,mapT,mapR,mapT,wall,thick);line(c,mapL,mapT,mapL,mapB,wall,thick);line(c,mapR,mapT,mapR,mapB,wall,thick);
            line(c,mapL,mapB,mapL+houseW*.385f,mapB,wall,thick);line(c,mapL+houseW*.615f,mapB,mapR,mapB,wall,thick);
            // Internal doorways at each orthogonal room connection.
            float gap=cell*.19f;
            for(int x=1;x<world.cols;x++)for(int y=0;y<world.rows;y++){float xx=mapL+x*cell,yy=mapT+y*cell;line(c,xx,yy,xx,yy+cell*.405f,wall,thick);line(c,xx,yy+cell*.595f,xx,yy+cell,wall,thick);line(c,xx-thick*.55f,yy+cell*.405f,xx+thick*.55f,yy+cell*.405f,edge,1.2f);line(c,xx-thick*.55f,yy+cell*.595f,xx+thick*.55f,yy+cell*.595f,edge,1.2f);}
            for(int y=1;y<world.rows;y++)for(int x=0;x<world.cols;x++){float xx=mapL+x*cell,yy=mapT+y*cell;line(c,xx,yy,xx+cell*.405f,yy,wall,thick);line(c,xx+cell*.595f,yy,xx+cell,yy,wall,thick);line(c,xx+cell*.405f,yy-thick*.55f,xx+cell*.405f,yy+thick*.55f,edge,1.2f);line(c,xx+cell*.595f,yy-thick*.55f,xx+cell*.595f,yy+thick*.55f,edge,1.2f);}
            // Exterior windows on deterministic wall positions.
            for(int x=0;x<world.cols;x++){if((x%2)==0){float xx=mapL+(x+.5f)*cell;drawWindow(c,xx-cell*.16f,mapT-thick*.5f,xx+cell*.16f,mapT+thick*.7f,true);}}
            for(int y=0;y<world.rows;y++){if((y%2)==0){float yy=mapT+(y+.5f)*cell;drawWindow(c,mapR-thick*.5f,yy-cell*.15f,mapR+thick*.7f,yy+cell*.15f,false);}}
            // Trim outline and a small door leaf / hinge detail.
            strokeRect(c,mapL-1,mapT-1,mapR+1,mapB+1,0xffe0cba9,1.4f,2);
            line(c,px(.5f),mapB,px(.5f)+cell*.08f,mapB-cell*.06f,0xffd1b28a,2f);circle(c,px(.5f)+cell*.075f,mapB-cell*.055f,2.2f,0xffffd78b);
        }
        private void drawWindow(Canvas c,float l,float t,float r,float b,boolean horizontal){bgRect(c,l-2,t-2,r+2,b+2,0xff544638,2);bgRect(c,l,t,r,b,0xff8fdbef,2);rect(c,l+(r-l)*.45f,t,l+(r-l)*.55f,b,0xffd7f6ff);rect(c,l,t+(b-t)*.43f,r,t+(b-t)*.57f,0xffd7f6ff);rect(c,l+2,t+2,r-2,t+(b-t)*.18f,0x55ffffff);}
        private void drawFurniture(Canvas c,WorldModel.Furniture f){
            float l=mapL+(f.roomX+f.x)*cell,t=mapT+(f.roomY+f.y)*cell,r=l+f.w*cell,b=t+f.h*cell,w=r-l,h=b-t;
            // Contact shadow is offset and separate from the object body.
            if(f.solid)bgRect(c,l+2,t+3,r+3,b+4,0x35000000,Math.max(3,cell*.025f));
            switch(f.type){
                case WorldModel.SOFA:
                    bgRect(c,l,t+h*.18f,r,b,0xff5d514c,Math.max(3,h*.13f));bgRect(c,l+2,t+2,r-2,b-h*.12f,0xff648b86,Math.max(3,h*.16f));bgRect(c,l+w*.06f,t+h*.24f,l+w*.30f,b-h*.18f,0xff81aaa3,3);bgRect(c,l+w*.35f,t+h*.24f,l+w*.64f,b-h*.18f,0xff729a94,3);bgRect(c,l+w*.69f,t+h*.24f,r-w*.06f,b-h*.18f,0xff81aaa3,3);line(c,l+w*.32f,t+h*.24f,l+w*.32f,b-h*.18f,0x80617770,1);line(c,l+w*.66f,t+h*.24f,l+w*.66f,b-h*.18f,0x80617770,1);break;
                case WorldModel.TV:
                    bgRect(c,l,t,r,b,0xff282d35,3);bgRect(c,l+w*.06f,t+h*.09f,r-w*.06f,b-h*.19f,0xff192936,2);rect(c,l+w*.12f,t+h*.16f,r-w*.18f,t+h*.22f,0x445bd6e8);line(c,l+w*.5f,b-h*.16f,l+w*.5f,b+h*.02f,0xff343940,2);break;
                case WorldModel.FRIDGE:
                    bgRect(c,l,t,r,b,0xffb9c6c7,4);strokeRect(c,l+1,t+1,r-1,b-1,0xffedf4ef,1,4);line(c,l+w*.05f,t+h*.42f,r-w*.05f,t+h*.42f,0xff899a9c,1.2f);line(c,r-w*.15f,t+h*.11f,r-w*.15f,t+h*.34f,0xff6f8284,2);line(c,r-w*.15f,t+h*.50f,r-w*.15f,t+h*.79f,0xff6f8284,2);break;
                case WorldModel.COUNTER:
                    bgRect(c,l,t,r,b,0xff876d55,3);bgRect(c,l-1,t-2,r+1,t+h*.20f,0xffe6d7bc,3);line(c,l+w*.5f,t+h*.23f,l+w*.5f,b,0xff5c4939,1.3f);line(c,l+w*.12f,t+h*.30f,l+w*.37f,t+h*.30f,0xffc2a889,1.3f);line(c,l+w*.62f,t+h*.30f,l+w*.87f,t+h*.30f,0xffc2a889,1.3f);break;
                case WorldModel.TABLE:
                    bgRect(c,l+w*.08f,t+h*.12f,r-w*.08f,b,0xff8a6040,3);bgRect(c,l,t,r,b-h*.12f,0xffc79a64,3);oval(c,l+w*.36f,t+h*.16f,l+w*.58f,t+h*.54f,0xfff4eee0);circle(c,l+w*.46f,t+h*.34f,Math.max(1.5f,w*.025f),0xffd98e60);break;
                case WorldModel.BED:
                    bgRect(c,l,t,r,b,0xff644b3f,4);bgRect(c,l+w*.04f,t+h*.04f,r-w*.04f,b-h*.04f,0xffe2d6c5,4);bgRect(c,l+w*.06f,t+h*.06f,r-w*.06f,b-h*.15f,0xff6d91b1,4);bgRect(c,l+w*.08f,t+h*.08f,l+w*.42f,t+h*.28f,0xfff6f0df,3);line(c,l+w*.52f,t+h*.25f,l+w*.52f,b-h*.18f,0x777fa2bc,1);bgRect(c,l+w*.08f,b-h*.13f,r-w*.08f,b-h*.04f,0xff8eabc2,2);break;
                case WorldModel.WARDROBE:
                    bgRect(c,l,t,r,b,0xff87684f,3);strokeRect(c,l+1,t+1,r-1,b-1,0xffbc9978,1,3);line(c,l+w*.5f,t+2,l+w*.5f,b-2,0xff5e4638,1.2f);circle(c,l+w*.43f,t+h*.52f,Math.max(1,w*.025f),0xffe0c18e);circle(c,l+w*.57f,t+h*.52f,Math.max(1,w*.025f),0xffe0c18e);break;
                case WorldModel.BATH:
                    bgRect(c,l,t,r,b,0xff687e85,5);bgRect(c,l+w*.06f,t+h*.10f,r-w*.06f,b-h*.10f,0xffd9ecec,6);bgRect(c,l+w*.13f,t+h*.18f,r-w*.13f,b-h*.18f,0xff95d5e2,5);oval(c,l+w*.2f,t+h*.27f,l+w*.34f,t+h*.43f,0xffe5f9fc);break;
                case WorldModel.SINK:
                    bgRect(c,l,t+h*.25f,r,b,0xff8b9292,3);bgRect(c,l-1,t,r+1,t+h*.34f,0xfff0f3ee,4);oval(c,l+w*.15f,t+h*.08f,r-w*.15f,t+h*.27f,0xff9cd0d7);circle(c,l+w*.5f,t+h*.17f,Math.max(1,w*.025f),0xff4a7781);break;
                case WorldModel.TOILET:
                    bgRect(c,l+w*.20f,t,r-w*.20f,t+h*.42f,0xfff0f0e8,3);oval(c,l,t+h*.22f,r,b,0xfff0f2e9);oval(c,l+w*.18f,t+h*.35f,r-w*.18f,b-h*.18f,0xff9ccbd3);break;
                case WorldModel.DESK:
                    bgRect(c,l,t,r,b,0xff8b6248,3);bgRect(c,l+w*.10f,t+h*.06f,r-w*.10f,t+h*.24f,0xffb98961,2);bgRect(c,l+w*.38f,t-h*.03f,l+w*.67f,t+h*.08f,0xff303c4a,2);rect(c,l+w*.42f,t,l+w*.63f,t+h*.045f,0xff4ea2ad);break;
                case WorldModel.SHELF:
                    bgRect(c,l,t,r,b,0xff745540,3);for(int sy=1;sy<4;sy++){line(c,l+2,t+h*sy*.25f,r-2,t+h*sy*.25f,0xffd4b28a,2);for(int j=0;j<3;j++){float bl=l+w*(.07f+j*.28f),br=bl+w*.18f;bgRect(c,bl,t+h*(sy*.25f-.12f),Math.min(r-2,br),t+h*(sy*.25f-.02f),SHELF_COLORS[(sy+j)%SHELF_COLORS.length],1);}}break;
                case WorldModel.CABINET:
                    bgRect(c,l,t,r,b,0xffb28c68,3);strokeRect(c,l+1,t+1,r-1,b-1,0xffe1c7a5,1,3);line(c,l+w*.5f,t+2,l+w*.5f,b-2,0xff806348,1);circle(c,l+w*.42f,t+h*.5f,Math.max(1,w*.025f),0xffead0a1);circle(c,l+w*.58f,t+h*.5f,Math.max(1,w*.025f),0xffead0a1);break;
                case WorldModel.PLANT:
                    bgRect(c,l+w*.18f,t+h*.54f,r-w*.18f,b,0xffb16e48,3);line(c,l+w*.5f,t+h*.65f,l+w*.5f,t+h*.12f,0xff4c8c55,2);oval(c,l+w*.14f,t+h*.22f,l+w*.51f,t+h*.48f,0xff4b9258);oval(c,l+w*.45f,t+h*.14f,l+w*.90f,t+h*.42f,0xff6bbd6a);oval(c,l+w*.30f,t+h*.02f,l+w*.70f,t+h*.29f,0xff38864e);break;
                case WorldModel.RUG:
                    bgRect(c,l,t,r,b,0xffbd9274,4);strokeRect(c,l+2,t+2,r-2,b-2,0xffe5c2a0,1.5f,3);strokeRect(c,l+w*.10f,t+h*.12f,r-w*.10f,b-h*.12f,0x77f7dec0,1,2);for(int k=1;k<5;k++)line(c,l+w*k/5f,t+h*.12f,l+w*k/5f,b-h*.12f,0x22fff4da,1);break;
                case WorldModel.CHAIR:
                    bgRect(c,l+w*.16f,t+h*.08f,r-w*.16f,t+h*.60f,0xff9b7658,3);bgRect(c,l,t+h*.40f,r,b-h*.06f,0xffbe976f,3);line(c,l+w*.17f,b-h*.02f,l+w*.17f,b+h*.04f,0xff73523a,2);line(c,r-w*.17f,b-h*.02f,r-w*.17f,b+h*.04f,0xff73523a,2);break;
                case WorldModel.WORKBENCH:
                    bgRect(c,l,t+h*.18f,r,b,0xff635f5b,3);bgRect(c,l-1,t-1,r+1,t+h*.24f,0xffc2a17a,3);line(c,l+w*.10f,t+h*.37f,l+w*.85f,t+h*.37f,0xffd6bb96,1);for(int k=0;k<4;k++)rect(c,l+w*(.12f+k*.19f),t+h*.05f,l+w*(.20f+k*.19f),t+h*.16f,WORKBENCH_COLORS[k]);break;
                case WorldModel.GREENHOUSE:
                    bgRect(c,l,t,r,b,0x553d9aa2,3);strokeRect(c,l,t,r,b,0xffedf1d9,2,3);line(c,l+w*.5f,t,l+w*.5f,b,0xffdcebd4,1.3f);line(c,l,t+h*.5f,r,t+h*.5f,0xffdcebd4,1.3f);oval(c,l+w*.10f,t+h*.18f,l+w*.38f,t+h*.45f,0xff559d68);oval(c,l+w*.56f,t+h*.52f,l+w*.89f,t+h*.79f,0xff6dbb77);break;
            }
        }
        private void drawFood(Canvas c){for(WorldModel.Food f:world.food)if(f.active){float x=px(f.x),y=py(f.y),r=Math.max(4,cell*.035f);oval(c,x-r*1.25f,y-r*.20f,x+r*1.15f,y+r*.60f,0x40000000);oval(c,x-r,y-r*.45f,x+r,y+r*.42f,0xffd5794c);oval(c,x-r*.65f,y-r*.30f,x+r*.52f,y+r*.15f,0xfff0b16b);circle(c,x-r*.28f,y-r*.26f,r*.12f,0xffffe3a6);line(c,x+r*.4f,y-r*.45f,x+r*.7f,y-r*.85f,0xff63915a,Math.max(1.2f,cell*.008f));}}
        private void drawFly(Canvas c){
            float x=px(fx),y=py(fy);Skin s=SKINS[fly.skin];float r=Math.max(7f,cell*.065f);float angle=(float)Math.toDegrees(Math.atan2(vy,vx));if(Math.abs(vx)+Math.abs(vy)<.001f)angle=0;
            // Drop shadow and animated translucent wings.
            oval(c,x-r*1.7f,y+r*.30f,x+r*1.4f,y+r*.88f,0x55000000);
            c.save();c.translate(x,y);c.rotate(angle);float flap=(float)Math.sin(wingPhase)*r*.22f;
            oval(c,-r*.70f,-r*1.82f-flap,r*.30f,-r*.22f-flap,0x88ffffff);oval(c,-r*.48f,-r*1.72f-flap,r*.22f,-r*.36f-flap,s.wing);oval(c,-r*.60f,r*.18f+flap,r*.33f,r*1.63f+flap,0x88ffffff);oval(c,-r*.44f,r*.30f+flap,r*.24f,r*1.43f+flap,s.wing);
            // Thin wing veins add detail while the translucent ovals keep the wings readable.
            line(c,-r*.50f,-r*1.52f,r*.05f,-r*.74f,0x667b9ba4,Math.max(.7f,r*.035f));line(c,-r*.52f,-r*1.22f,r*.14f,-r*.58f,0x557b9ba4,Math.max(.7f,r*.03f));line(c,-r*.43f,r*1.30f,r*.10f,r*.62f,0x667b9ba4,Math.max(.7f,r*.035f));line(c,-r*.45f,r*1.03f,r*.16f,r*.50f,0x557b9ba4,Math.max(.7f,r*.03f));
            // Three pairs of legs with tiny joints.
            int limb=0xff302b2a;for(int k=0;k<3;k++){float hx=-r*.20f+k*r*.43f;float by=r*.15f+(k-1)*r*.12f;line(c,hx,by,hx-r*.18f,by+r*.46f,limb,Math.max(1.1f,r*.075f));line(c,hx-r*.18f,by+r*.46f,hx-r*.37f,by+r*.67f,limb,Math.max(1.1f,r*.065f));line(c,hx,by,hx+r*.20f,by+r*.43f,limb,Math.max(1.1f,r*.075f));line(c,hx+r*.20f,by+r*.43f,hx+r*.38f,by+r*.62f,limb,Math.max(1.1f,r*.065f));}
            // Segmented abdomen, thorax and head.
            oval(c,-r*1.55f,-r*.47f,-r*.12f,r*.48f,s.body);oval(c,-r*1.39f,-r*.35f,-r*.18f,-r*.19f,s.accent);oval(c,-r*1.25f,-r*.05f,-r*.30f,r*.09f,0x776f4b39);oval(c,-r*.98f,-r*.06f,-r*.63f,r*.09f,0x665a4035);
            // A handful of skins have their own markings, not just a different base hue.
            if(fly.skin==1||fly.skin==7||fly.skin==9){for(int k=0;k<3;k++)line(c,-r*(1.22f-k*.25f),-r*.33f,-r*(1.13f-k*.25f),r*.33f,fly.skin==9?0xff65292c:s.accent,Math.max(1f,r*.07f));}
            else if(fly.skin==2||fly.skin==8){circle(c,-r*1.12f,-r*.12f,r*.08f,s.accent);circle(c,-r*.82f,r*.18f,r*.07f,s.accent);circle(c,-r*.58f,-r*.22f,r*.06f,s.accent);}
            else if(fly.skin==10){circle(c,-r*1.17f,-r*.19f,r*.055f,0xfffff1b5);circle(c,-r*.91f,r*.18f,r*.045f,0xffa9a7ff);circle(c,-r*.65f,-r*.22f,r*.04f,0xfffff1b5);}
            else if(fly.skin==11){line(c,-r*1.30f,-r*.25f,-r*1.22f,r*.22f,0xffff7cae,Math.max(1f,r*.055f));line(c,-r*1.02f,-r*.29f,-r*.94f,r*.25f,0xffffdf7e,Math.max(1f,r*.055f));line(c,-r*.76f,-r*.27f,-r*.68f,r*.22f,0xff6be4c5,Math.max(1f,r*.055f));}
            oval(c,-r*.34f,-r*.58f,r*.54f,r*.57f,s.thorax);circle(c,r*.63f,0,r*.47f,s.body);circle(c,r*.72f,-r*.11f,r*.12f,0xff14171a);circle(c,r*.72f,r*.12f,r*.12f,0xff14171a);circle(c,r*.78f,-r*.15f,r*.045f,0xffffffff);
            line(c,r*.80f,-r*.33f,r*1.10f,-r*.72f,0xff282a29,Math.max(1,r*.055f));line(c,r*.82f,r*.33f,r*1.08f,r*.62f,0xff282a29,Math.max(1,r*.055f));circle(c,r*1.10f,-r*.72f,r*.05f,0xff252525);circle(c,r*1.08f,r*.62f,r*.05f,0xff252525);
            oval(c,-r*1.15f,-r*.35f,-r*.78f,-r*.12f,0x77ffffff);oval(c,-r*.08f,-r*.40f,r*.10f,-r*.18f,0x88ffffff);c.restore();
            if(mode==MODE_TRAIN&&targetActive||mode==MODE_LAB&&targetActive){float mx=px(targetMarkerX),my=py(targetMarkerY),rr=r*1.8f;p.setStyle(Paint.Style.STROKE);p.setColor(0xffffd66f);p.setStrokeWidth(2);c.drawCircle(mx,my,rr,p);c.drawLine(mx-rr*.5f,my,mx+rr*.5f,my,p);c.drawLine(mx,my-rr*.5f,mx,my+rr*.5f,p);p.setStyle(Paint.Style.FILL);}
            // Memory cloud: the visited-room count is tied to real room entries.
            if(cell>100){float cx=x,cy=y-r*2.15f;circle(c,cx,cy,r*.20f,0x8870d5b2);circle(c,cx-r*.24f,cy+r*.10f,r*.11f,0x886fe4bf);}
        }
        private void drawWorldHeader(Canvas c){bgRect(c,W*.034f,H*.045f,W*.697f,H*.125f,0xe51e2b3b,10);text(c,"FLYLIFE",W*.052f,H*.081f,H*.028f,textColor);text(c,"1.0 RELEASE  ·  2D LIFE SIM",W*.052f,H*.105f,H*.0135f,mint);String house=HOUSE_NAMES[world.houseType];text(c,house,W*.27f,H*.077f,H*.019f,gold);fitText(c,"SEED  "+seed,W*.27f,H*.104f,W*.30f,H*.015f,muted);float val=W*.60f;centerText(c,"КОМНАТЫ",val,H*.073f,H*.012f,muted);centerText(c,Integer.bitCount(runVisited)+" / "+world.rooms.length,val,H*.104f,H*.022f,mint);}
        private void drawPanel(Canvas c){float l=W*.738f,r=W*.976f,w=r-l;bgRect(c,l,H*.04f,r,H*.145f,0xff202e3f,10);text(c,"МОЯ МУХА",l+w*.06f,H*.075f,H*.020f,textColor);fitText(c,fly.name+"  ·  Ур. "+fly.level,l+w*.06f,H*.106f,w*.88f,H*.017f,gold);fitText(c,"XP "+fly.xp+"   ·   "+traitName(fly.trait),l+w*.06f,H*.131f,w*.88f,H*.014f,muted);
            text(c,"МОНЕТЫ  "+coins,l+w*.05f,H*.177f,H*.018f,gold);text(c,"ГОЛОД",l+w*.05f,H*.204f,H*.012f,muted);drawBar(c,l+w*.37f,H*.191f,w*.57f,H*.018f,hunger,0xffe6a25e);text(c,"ЭНЕРГИЯ",l+w*.05f,H*.235f,H*.012f,muted);drawBar(c,l+w*.37f,H*.222f,w*.57f,H*.018f,energy,0xff68cda5);
            float start=H*.267f,bh=H*.0505f,gap=H*.0095f;
            for(int i=0;i<MODE_NAMES.length;i++){float y=start+i*(bh+gap);boolean selected=mode==i;int fill=selected?0xff286f68:0xff27374a;bgRect(c,l+w*.035f,y,r-w*.035f,y+bh,fill,7);if(selected)bgRect(c,l+w*.035f,y,l+w*.055f,y+bh,mint,3);fitText(c,MODE_NAMES[i],l+w*.09f,y+bh*.64f,w*.83f,H*.016f,selected?0xffffffff:0xffd1dcea);}
            String[] labels={"ПАСЕКА","ДОСТИЖЕНИЯ","СОХРАНИТЬ","НОВЫЙ МИР","ПАУЗА","НАСТРОЙКИ"};float base=H*.635f,bh2=H*.061f,gapY=H*.012f,gapX=w*.035f,bw=(w-w*.10f)/2;
            for(int i=0;i<labels.length;i++){int row=i/2,col=i%2;float x=l+w*.035f+col*(bw+gapX),y=base+row*(bh2+gapY);bgRect(c,x,y,x+bw,y+bh2,0xff34475c,7);fitText(c,labels[i],x+5,y+bh2*.64f,bw-10,H*.0132f,textColor);}
            text(c,"Состояние: "+flyState,l+w*.045f,H*.875f,H*.012f,mint);text(c,"Касайся пола, чтобы задать цель",l+w*.045f,H*.900f,H*.011f,muted);text(c,"v1.0.0 · OFFLINE",l+w*.045f,H*.936f,H*.012f,0xff6f8298);
        }
        private void drawBar(Canvas c,float x,float y,float w,float h,float v,int col){bgRect(c,x,y,x+w,y+h,0xff38495b,4);if(v>0)bgRect(c,x,y,x+w*clamp(v,0,1),y+h,col,4);}
        private String traitName(int t){return TRAIT_NAMES[clampInt(t,0,5)];}
        private void drawToast(Canvas c,String msg){float boxW=Math.min(W*.63f,Math.max(W*.28f,msg.length()*H*.0085f));float x=(W-boxW)*.5f,y=H*.895f;bgRect(c,x,y,x+boxW,y+H*.055f,0xf2293b4f,12);strokeRect(c,x,y,x+boxW,y+H*.055f,0xff49667a,1,12);fitText(c,msg,x+14,y+H*.035f,boxW-28,H*.016f,gold);}
        private void drawEscapeOverlay(Canvas c){bgRect(c,W*.10f,H*.37f,W*.66f,H*.61f,0xf022332f,16);strokeRect(c,W*.10f,H*.37f,W*.66f,H*.61f,0xff7ad9b0,2,16);centerText(c,"ТЫ ВЫБРАЛАСЬ!",W*.38f,H*.445f,H*.045f,0xffb9f6d7);centerText(c,"Муха получила опыт и монеты.",W*.38f,H*.49f,H*.021f,textColor);centerText(c,"Нажми на «Побег» ещё раз для новой попытки.",W*.38f,H*.535f,H*.016f,muted);}
        private void drawHelpOverlay(Canvas c){bgRect(c,W*.08f,H*.20f,W*.69f,H*.79f,0xf51a2738,15);text(c,"КАК ИГРАТЬ",W*.12f,H*.27f,H*.035f,textColor);String[] lines={"• Один мир, одна муха, общие навыки и сохранение.","• В «Свободной жизни» муха сама ест, исследует дом и иногда тянется к свету у окон.","• В «Обучении» коснись пола — это станет целью.","• Новые комнаты дают опыт; еда восстанавливает силы.","• Сохрани муху в пасеку, чтобы продолжить позже.","• Настройки позволяют повторить мир по тому же seed.","• Real Brain — прозрачная упрощённая демонстрация,", "  а не подключение настоящего FlyWire-коннектома."};for(int i=0;i<lines.length;i++)text(c,lines[i],W*.12f,H*(.335f+i*.048f),H*.019f,i==7?gold:0xffd1dcea);bgRect(c,W*.48f,H*.70f,W*.64f,H*.755f,0xff2c514b,8);centerText(c,"ПОНЯТНО",W*.56f,H*.728f,H*.017f,mint);}
        private void drawBrainOverlay(Canvas c){float x=W*.045f,y=H*.78f,w=W*.62f,h=H*.13f;bgRect(c,x,y,x+w,y+h,0xd91b2a3a,10);text(c,"REAL BRAIN · СЕНСОРНАЯ ДЕМОНСТРАЦИЯ",x+12,y+H*.025f,H*.0135f,gold);String[] labels={"ЗАПАХ","ГОЛОД","ПАМЯТЬ","МОТОРИКА"};float odor=0;for(WorldModel.Food f:world.food)if(f.active)odor=Math.max(odor,clamp(.7f-dist(fx,fy,f.x,f.y)*2.5f,0,1));float[] vals={odor,hunger,Integer.bitCount(runVisited)/(float)Math.max(1,world.rooms.length),clamp((Math.abs(vx)+Math.abs(vy))*2f,0,1)};for(int i=0;i<4;i++){float xx=x+12+i*w*.245f;text(c,labels[i],xx,y+H*.052f,H*.0115f,muted);drawBar(c,xx,y+H*.066f,w*.19f,H*.017f,vals[i],i==brainPulse?gold:mint);}}

        private void drawApiary(Canvas c){c.drawColor(bg);bgRect(c,W*.025f,H*.035f,W*.975f,H*.965f,0xff192433,18);text(c,"ПАСЕКА",W*.055f,H*.09f,H*.044f,textColor);text(c,"Сохранённые мухи остаются частью твоего общего мира.",W*.055f,H*.135f,H*.019f,muted);text(c,"Коллекция: "+apiary.size()+" / 12",W*.78f,H*.09f,H*.022f,gold);float cw=W*.292f,ch=H*.133f,gx=W*.017f,gy=H*.014f,baseX=W*.045f,baseY=H*.185f;
            for(int i=0;i<12;i++){int col=i%3,row=i/3;float x=baseX+col*(cw+gx),y=baseY+row*(ch+gy);boolean has=i<apiary.size();bgRect(c,x,y,x+cw,y+ch,selectedFly==i?0xff245d57:0xff273649,10);if(selectedFly==i)strokeRect(c,x,y,x+cw,y+ch,mint,1.6f,10);if(has){FlyProfile f=apiary.get(i);drawMiniFly(c,x+cw*.10f,y+ch*.50f,Math.min(cw*.045f,H*.018f),f.skin);fitText(c,f.name,x+cw*.20f,y+ch*.32f,cw*.76f,H*.021f,textColor);text(c,"Уровень "+f.level+" · XP "+f.xp,x+cw*.20f,y+ch*.55f,H*.014f,muted);fitText(c,traitName(f.trait)+" · "+SKINS[f.skin].name,x+cw*.20f,y+ch*.78f,cw*.75f,H*.013f,mint);}else{centerText(c,"ПУСТО",x+cw*.5f,y+ch*.47f,H*.019f,0xff718399);centerText(c,"Сохрани муху в игре",x+cw*.5f,y+ch*.70f,H*.013f,0xff718399);}}
            button(c,"НАЗАД",W*.045f,H*.84f,W*.17f,H*.075f);button(c,"ВЕРНУТЬ МУХУ",W*.235f,H*.84f,W*.22f,H*.075f);button(c,"ВЫПУСТИТЬ",W*.475f,H*.84f,W*.19f,H*.075f);button(c,"ПЕРЕИМЕНОВАТЬ",W*.685f,H*.84f,W*.27f,H*.075f);text(c,"Пассивный доход: "+apiaryIncomePerMinute()+" монет/мин · офлайн доход ограничен 7 днями",W*.045f,H*.955f,H*.014f,muted);}
        private int apiaryIncomePerMinute(){int n=0;for(FlyProfile f:apiary)n+=Math.max(1,1+f.level/2);return n;}
        private void drawMiniFly(Canvas c,float x,float y,float r,int skinId){Skin s=SKINS[clampInt(skinId,0,SKINS.length-1)];oval(c,x-r*1.4f,y-r*.35f,x+r*1.3f,y+r*.55f,0x44000000);oval(c,x-r*.8f,y-r*1.7f,x+r*.1f,y-r*.2f,0x889de8f1);oval(c,x-r*.6f,y-r*1.45f,x+r*.2f,y-r*.3f,s.wing);oval(c,x-r*.8f,y+r*.2f,x+r*.1f,y+r*1.5f,0x889de8f1);oval(c,x-r*.5f,y+r*.25f,x+r*.2f,y+r*1.2f,s.wing);oval(c,x-r*1.25f,y-r*.38f,x+r*.15f,y+r*.38f,s.body);oval(c,x-r*.25f,y-r*.45f,x+r*.45f,y+r*.45f,s.thorax);circle(c,x+r*.53f,y,r*.34f,s.body);circle(c,x+r*.62f,y-r*.08f,r*.08f,0xff14171a);}
        private void drawAchievements(Canvas c){c.drawColor(bg);bgRect(c,W*.025f,H*.035f,W*.975f,H*.965f,0xff192433,18);text(c,"ДОСТИЖЕНИЯ И СКИНЫ",W*.05f,H*.09f,H*.039f,textColor);text(c,"Достижения дают +15 монет. Скины сохраняются навсегда.",W*.05f,H*.13f,H*.017f,muted);
            // Four columns keep all thirteen achievement cards clear of the skin shop below.
            float cw=W*.218f,ch=H*.078f,gx=W*.010f,gy=H*.009f,baseX=W*.045f,baseY=H*.16f;
            for(int i=0;i<ACH_NAMES.length;i++){int col=i%4,row=i/4;float x=baseX+col*(cw+gx),y=baseY+row*(ch+gy);boolean done=(achievementMask&(1<<i))!=0;bgRect(c,x,y,x+cw,y+ch,done?0xff24483f:0xff253448,8);circle(c,x+cw*.060f,y+ch*.50f,H*.013f,done?mint:0xff5d7188);centerText(c,done?"✓":"·",x+cw*.060f,y+ch*.50f+H*.006f,H*.017f,done?0xff12352e:textColor);fitText(c,ACH_NAMES[i],x+cw*.12f,y+ch*.38f,cw*.84f,H*.0145f,textColor);fitText(c,done?"Выполнено":ACH_DESC[i],x+cw*.12f,y+ch*.69f,cw*.84f,H*.0105f,done?mint:muted);}
            text(c,"СКИНЫ МУХИ  ·  ОТКРЫВАЙ И ВЫБИРАЙ",W*.05f,H*.565f,H*.018f,gold);float rowY1=H*.63f,rowY2=H*.72f;for(int i=0;i<SKINS.length;i++){int col=i%6,row=i/6;float x=W*(.085f+col*.145f),y=row==0?rowY1:rowY2;boolean owned=(skinMask&(1<<i))!=0;if(fly.skin==i){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.4f);p.setColor(gold);c.drawCircle(x,y,H*.029f,p);p.setStyle(Paint.Style.FILL);}drawMiniFly(c,x,y,H*.014f,i);fitText(c,SKINS[i].name,x-W*.045f,y+H*.033f,W*.09f,H*.012f,owned?textColor:muted);if(!owned)centerText(c,""+SKINS[i].cost,x,y+H*.050f,H*.011f,gold);}
            button(c,"НАЗАД",W*.78f,H*.875f,W*.17f,H*.065f);text(c,"Монеты: "+coins,W*.05f,H*.91f,H*.016f,gold);
        }
        private void drawSettings(Canvas c){c.drawColor(bg);bgRect(c,W*.025f,H*.035f,W*.975f,H*.965f,0xff192433,18);text(c,"МИР И НАСТРОЙКИ",W*.055f,H*.10f,H*.041f,textColor);text(c,"Генератор воспроизводим: одинаковый seed создаёт тот же дом, комнаты, мебель и еду.",W*.055f,H*.15f,H*.018f,muted);
            bgRect(c,W*.055f,H*.22f,W*.945f,H*.32f,0xff253448,10);text(c,"ТЕКУЩИЙ SEED",W*.075f,H*.252f,H*.014f,muted);fitText(c,Long.toString(seed),W*.075f,H*.29f,W*.55f,H*.025f,gold);text(c,"Тип: "+new String[]{"Семейный дом","Большой дом","Усадьба","Особняк"}[world.houseType]+" · "+world.cols*world.rows+" комнат",W*.63f,H*.282f,H*.017f,textColor);
            float y=H*.37f,h=H*.075f;button(c,"ВВЕСТИ SEED",W*.055f,y,W*.205f,h);button(c,"СЛУЧАЙНЫЙ МИР",W*.28f,y,W*.22f,h);button(c,"СБРОСИТЬ НУЖДЫ",W*.52f,y,W*.21f,h);button(c,"СБРОСИТЬ СОХРАНЕНИЕ",W*.75f,y,W*.195f,h);
            bgRect(c,W*.055f,H*.49f,W*.945f,H*.73f,0xff222f40,12);text(c,"ПАРАМЕТРЫ МУХИ",W*.08f,H*.54f,H*.025f,textColor);text(c,"Любопытство",W*.08f,H*.59f,H*.016f,muted);drawBar(c,W*.27f,H*.572f,W*.22f,H*.023f,fly.curiosity,mint);text(c,"Осторожность",W*.55f,H*.59f,H*.016f,muted);drawBar(c,W*.73f,H*.572f,W*.16f,H*.023f,fly.caution,gold);text(c,"Ген скорости",W*.08f,H*.65f,H*.016f,muted);drawBar(c,W*.27f,H*.632f,W*.22f,H*.023f,fly.speedGene,0xff7bc6ef);text(c,"Энергия",W*.55f,H*.65f,H*.016f,muted);drawBar(c,W*.73f,H*.632f,W*.16f,H*.023f,energy,0xff7bc6ef);text(c,"FlyWire не подключён: Real Brain показывает учебную сенсомоторную модель.",W*.08f,H*.70f,H*.014f,gold);
            text(c,"Всего еды: "+totalMeals+"   Побеги: "+escapes+"   Учебные цели: "+trainingTasks+"   Миры: "+worldsSeen,W*.06f,H*.80f,H*.018f,muted);button(c,"НАЗАД",W*.055f,H*.85f,W*.18f,H*.075f);button(c,"СПРАВКА",W*.26f,H*.85f,W*.18f,H*.075f);button(c,"СОХРАНИТЬ",W*.465f,H*.85f,W*.20f,H*.075f);}
        private void drawGuide(Canvas c){c.drawColor(bg);bgRect(c,W*.025f,H*.035f,W*.975f,H*.965f,0xff192433,18);text(c,"FLYLIFE 1.0 · ПОЛЕВОЙ СПРАВОЧНИК",W*.055f,H*.10f,H*.04f,textColor);String[] lines={"01  ОДИН ОБЩИЙ МИР","Дом, муха, опыт, монеты, скины и пасека работают вместе.","02  ГЕНЕРАЦИЯ SEED","Введите seed в настройках, чтобы точно восстановить планировку.","03  ПОВЕДЕНИЕ","Муха ищет пищу по реальному расстоянию, избегает мебели и планирует путь по проходам.","04  ОБУЧЕНИЕ И FLY LAB","Касание задаёт цель; агент прокладывает путь по сетке проходимости.","05  ЭВОЛЮЦИЯ И REAL BRAIN","Опыт меняет параметры поведения. Real Brain — объяснимая упрощённая модель, не настоящий connectome.","06  ПАСЕКА","Сохраняй до 12 мух. Их индивидуальные навыки, характер, уровень и кожа сохраняются.","07  ОГРАНИЧЕНИЯ РЕЛИЗА","Это самостоятельный 2D-релиз; мультиплеер, 3D и реальная модель FlyWire не входят в 1.0."};float y=H*.17f;for(int i=0;i<lines.length;i++){text(c,lines[i],W*.065f,y,H*(i%2==0?.020f:.016f),i%2==0?gold:0xffd1dcea);y+=H*(i%2==0?.037f:.050f);}button(c,"НАЗАД",W*.79f,H*.86f,W*.15f,H*.07f);}
        private void button(Canvas c,String s,float x,float y,float w,float h){bgRect(c,x,y,x+w,y+h,0xff34485e,8);strokeRect(c,x,y,x+w,y+h,0xff455d73,1,8);fitText(c,s,x+Math.max(7,w*.06f),y+h*.64f,w-Math.max(14,w*.12f),H*.016f,textColor);}

        private void saveFly(){if(apiary.size()>=12){notifyUser("Пасека заполнена (12 мух)");return;}FlyProfile f=fly.copy();String base=fly.name.replaceFirst("\\s+\\d{2}$", "").trim();if(base.isEmpty())base=fly.name;String candidate;int suffix=apiary.size()+1;do{candidate=base+" "+String.format(Locale.US,"%02d",suffix++);}while(apiaryNameExists(candidate));f.name=candidate;f.born=System.currentTimeMillis();f.visitedMask=runVisited;apiary.add(f);lastApiaryTick=SystemClock.uptimeMillis();unlock(3);if(apiary.size()>=3)unlock(5);persist();notifyUser("Муха "+f.name+" сохранена в пасеку");}
        private boolean apiaryNameExists(String name){for(FlyProfile f:apiary)if(f.name.equalsIgnoreCase(name))return true;return false;}
        private void recallFly(){if(selectedFly<0||selectedFly>=apiary.size()){notifyUser("Сначала выбери муху в пасеке");return;}FlyProfile selected=apiary.remove(selectedFly);fly=selected.copy();fly.name=selected.name;hunger=.22f;energy=Math.max(.55f,fly.stamina);spawnAtStart();runVisited=fly.visitedMask;lastRoom=-1;escaped=false;targetActive=false;screen=0;selectedFly=-1;lastApiaryTick=SystemClock.uptimeMillis();persist();notifyUser("Возвращена муха "+fly.name+" — профиль восстановлен");}
        private void releaseFly(){if(selectedFly<0||selectedFly>=apiary.size()){notifyUser("Сначала выбери муху");return;}String n=apiary.get(selectedFly).name;apiary.remove(selectedFly);selectedFly=-1;lastApiaryTick=SystemClock.uptimeMillis();persist();notifyUser(n+" выпущена на волю");}
        private void renameSelectedFly(){if(selectedFly<0||selectedFly>=apiary.size()){notifyUser("Сначала выбери муху");return;}final EditText input=new EditText(MainActivity.this);input.setSingleLine(true);input.setText(apiary.get(selectedFly).name);new AlertDialog.Builder(MainActivity.this).setTitle("Имя мухи").setView(input).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{String n=input.getText().toString().trim();if(!n.isEmpty()){apiary.get(selectedFly).name=n.substring(0,Math.min(22,n.length()));persist();notifyUser("Имя сохранено");}invalidate();}).show();}
        private void showSeedDialog(){final EditText input=new EditText(MainActivity.this);input.setSingleLine(true);input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);input.setHint("Например: 123456789");input.setText(Long.toString(seed));new AlertDialog.Builder(MainActivity.this).setTitle("Seed мира").setMessage("Одинаковое число воспроизводит ту же планировку.").setView(input).setNegativeButton("Отмена",null).setPositiveButton("Создать мир",(d,w)->{try{long value=Long.parseLong(input.getText().toString().trim());screen=0;newWorld(value);}catch(Exception ex){notifyUser("Некорректный seed");}invalidate();}).show();}
        private void confirmReset(){new AlertDialog.Builder(MainActivity.this).setTitle("Удалить сохранение?").setMessage("Будут удалены монеты, мухи в пасеке, скины и прогресс. Это действие нельзя отменить.").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(d,w)->{prefs.edit().clear().apply();game=null;recreate();}).show();}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();if(W<=0||H<=0){W=getWidth();H=getHeight();}if(screen==1){handleApiaryTouch(x,y);invalidate();return true;}if(screen==2){handleAchievementTouch(x,y);invalidate();return true;}if(screen==3){handleSettingsTouch(x,y);invalidate();return true;}if(screen==4){screen=0;invalidate();return true;}
            if(showHelp){showHelp=false;invalidate();return true;}if(x>W*.728f){float l=W*.738f,w=W*.238f,start=H*.267f,bh=H*.0505f,gap=H*.0095f;int idx=(int)((y-start)/(bh+gap));if(y>=start&&y<start+6*(bh+gap)&&idx>=0&&idx<6){boolean wasEscaped=escaped;mode=idx;paused=false;if(wasEscaped){escaped=false;spawnAtStart();targetActive=false;}if(mode==MODE_ESCAPE){if(escaped)spawnAtStart();escaped=false;tx=.5f;ty=1.025f;lastDecision=0;invalidateRoute();}else if(mode==MODE_TRAIN||mode==MODE_LAB){targetActive=false;flyState="Ждёт цели";}else lastDecision=0;showBrainPanel=mode==MODE_BRAIN;notifyUser(MODE_NAMES[mode]);dirty=true;persist();invalidate();return true;}
                float base=H*.635f,bh2=H*.061f,gapY=H*.012f;int row=(int)((y-base)/(bh2+gapY)),col=x<(l+w*.5f)?0:1;if(y>=base&&row>=0&&row<3&&y<base+3*(bh2+gapY)){int action=row*2+col;switch(action){case 0:screen=1;selectedFly=-1;break;case 1:screen=2;break;case 2:saveFly();break;case 3:newWorld(random.nextLong());break;case 4:paused=!paused;notifyUser(paused?"Пауза":"Игра продолжается");break;default:screen=3;break;}invalidate();return true;}if(y>H*.88f){screen=4;invalidate();return true;}return true;
            }
            if(x>=mapL&&x<=mapR&&y>=mapT&&y<=mapB){float gx=clamp((x-mapL)/houseW,.02f,.98f),gy=clamp((y-mapT)/houseH,.02f,.98f);if(mode==MODE_TRAIN||mode==MODE_LAB){float[] adjusted=world.resolveTarget(gx,gy);targetMarkerX=adjusted[0];targetMarkerY=adjusted[1];tx=targetMarkerX;ty=targetMarkerY;targetActive=true;lastDecision=SystemClock.uptimeMillis();notifyUser(mode==MODE_TRAIN?"Учебная цель установлена":"Лабораторная цель установлена");}else if(showBrainPanel){showBrainPanel=false;}else{showHelp=true;}dirty=true;persist();invalidate();return true;}
            showHelp=true;invalidate();return true;}
        private void handleApiaryTouch(float x,float y){if(y>H*.83f){if(x<W*.22f){screen=0;}else if(x<W*.465f){recallFly();}else if(x<W*.68f){releaseFly();}else renameSelectedFly();return;}float baseY=H*.185f,ch=H*.133f,gy=H*.014f,baseX=W*.045f,cw=W*.292f,gx=W*.017f;if(x>=baseX&&y>=baseY&&y<baseY+4*(ch+gy)){int col=(int)((x-baseX)/(cw+gx)),row=(int)((y-baseY)/(ch+gy));int idx=row*3+col;if(col>=0&&col<3&&row>=0&&row<4&&idx<apiary.size())selectedFly=idx;}}
        private void handleAchievementTouch(float x,float y){if(y>H*.86f&&x>W*.72f){screen=0;return;}float rowY1=H*.63f,rowY2=H*.72f;if(y>rowY1-H*.035f&&y<rowY2+H*.055f){int row=y<H*.70f?0:1;int idx=Math.round((x/W-.085f)/.145f)+row*6;if(idx<0||idx>=SKINS.length)return;if((skinMask&(1<<idx))==0){if(coins<SKINS[idx].cost){notifyUser("Не хватает монет: нужно "+SKINS[idx].cost);return;}coins-=SKINS[idx].cost;skinMask|=1<<idx;notifyUser("Открыт скин "+SKINS[idx].name);}fly.skin=idx;checkAchievements();persist();return;}}
        boolean handleSystemBack(){
            if(showHelp){showHelp=false;invalidate();return true;}
            if(screen!=0){screen=0;selectedFly=-1;persist();invalidate();return true;}
            if(mode==MODE_BRAIN&&showBrainPanel){showBrainPanel=false;invalidate();return true;}
            if(paused){paused=false;notifyUser("Игра продолжается");invalidate();return true;}
            return false;
        }
        private void handleSettingsTouch(float x,float y){if(y>H*.84f){if(x<W*.23f){screen=0;}else if(x<W*.45f){screen=4;}else persist();return;}if(y>H*.35f&&y<H*.46f){if(x<W*.27f)showSeedDialog();else if(x<W*.51f)newWorld(random.nextLong());else if(x<W*.745f){hunger=.12f;energy=1;notifyUser("Потребности восстановлены");persist();}else confirmReset();}}
    }
}
