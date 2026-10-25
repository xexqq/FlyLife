package com.flylife.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Deterministic procedural house, collision model and grid route planner. */
public final class WorldModel {
    public static final int LIVING = 0, KITCHEN = 1, BEDROOM = 2, BATHROOM = 3,
            HALL = 4, STUDY = 5, WORKSHOP = 6, CONSERVATORY = 7;
    public static final String[] ROOM_NAMES = {"Гостиная", "Кухня", "Спальня", "Ванная", "Прихожая", "Кабинет", "Мастерская", "Зимний сад"};
    public static final String[] ROOM_SHORT = {"ГОСТИНАЯ", "КУХНЯ", "СПАЛЬНЯ", "ВАННАЯ", "ПРИХОЖАЯ", "КАБИНЕТ", "МАСТЕРСКАЯ", "ЗИМНИЙ САД"};
    public static final int SOFA = 0, TV = 1, FRIDGE = 2, COUNTER = 3, TABLE = 4, BED = 5,
            WARDROBE = 6, BATH = 7, SINK = 8, TOILET = 9, DESK = 10, SHELF = 11,
            CABINET = 12, PLANT = 13, RUG = 14, CHAIR = 15, WORKBENCH = 16, GREENHOUSE = 17;
    public static final String[] FURNITURE_NAMES = {"Sofa", "Television", "Refrigerator", "Counter", "Dining table", "Bed", "Wardrobe", "Bathtub", "Sink", "Toilet", "Desk", "Bookshelf", "Cabinet", "Plant", "Rug", "Chair", "Workbench", "Greenhouse"};
    public static final int GRID = 10;
    public final long seed;
    public final int cols, rows, houseType;
    public final Room[] rooms;
    public final List<Furniture> furniture = new ArrayList<>();
    public final List<Food> food = new ArrayList<>();
    private final Random random;
    private final int gridW, gridH;
    private boolean[][] walkable;

    public static final class Room {
        public final int x, y, type;
        public Room(int x, int y, int type) { this.x = x; this.y = y; this.type = type; }
        public int index(int cols) { return y * cols + x; }
    }
    public static final class Furniture {
        public final int roomX, roomY, type;
        public final float x, y, w, h; // room-local 0..1 coordinates
        public final boolean solid;
        Furniture(int rx, int ry, int type, float x, float y, float w, float h, boolean solid) {
            this.roomX=rx; this.roomY=ry; this.type=type; this.x=x; this.y=y; this.w=w; this.h=h; this.solid=solid;
        }
        public float worldX(int cols) { return (roomX + x) / cols; }
        public float worldY(int rows) { return (roomY + y) / rows; }
        public float worldW(int cols) { return w / cols; }
        public float worldH(int rows) { return h / rows; }
    }
    public static final class Food {
        public float x, y;
        public boolean active = true;
        public long respawnAt;
        Food(float x, float y) { this.x=x; this.y=y; }
    }

    private WorldModel(long seed, int cols, int rows, int houseType) {
        this.seed=seed; this.cols=cols; this.rows=rows; this.houseType=houseType;
        this.rooms=new Room[cols*rows]; this.random=new Random(seed ^ 0x6A09E667F3BCC909L);
        this.gridW=cols*GRID; this.gridH=rows*GRID+1;
        generateRooms(); generateFurniture(); ensureNavigationConnectivity(); placeFood();
    }

    public static WorldModel generate(long seed) {
        Random r = new Random(mix64(seed));
        long mixedType=mix64(seed ^ 0x9E3779B97F4A7C15L);int type=(int)((mixedType%4L+4L)%4L);
        int[][] dims = {{3,2},{4,3},{5,3},{5,4}};
        return new WorldModel(seed, dims[type][0], dims[type][1], type);
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
    private void generateRooms() {
        for (int y=0;y<rows;y++) for (int x=0;x<cols;x++) {
            int type = random.nextInt(ROOM_NAMES.length);
            if (x==0 && y==0) type=LIVING;
            else if (x==cols-1 && y==0) type=KITCHEN;
            else if (x==0 && y==rows-1) type=BEDROOM;
            else if (x==cols-1 && y==rows-1) type=BATHROOM;
            else if (x==cols/2 && y==rows/2) type=HALL;
            rooms[y*cols+x]=new Room(x,y,type);
        }
    }
    private void add(int x,int y,int type,float px,float py,float w,float h,boolean solid) {
        // Small seeded placement offsets keep repeated room types from looking stamped out.
        float jitterX=(random.nextFloat()-.5f)*.024f;
        float jitterY=(random.nextFloat()-.5f)*.024f;
        float nx=Math.max(.055f,Math.min(.94f-w,px+jitterX));
        float ny=Math.max(.055f,Math.min(.94f-h,py+jitterY));
        furniture.add(new Furniture(x,y,type,nx,ny,w,h,solid));
    }
    private void generateFurniture() {
        for (Room room: rooms) {
            int x=room.x, y=room.y;
            switch (room.type) {
                case LIVING:
                    add(x,y,SOFA,.10f,.14f,.40f,.22f,true);
                    add(x,y,TV,.68f,.12f,.20f,.12f,true);
                    add(x,y,RUG,.32f,.47f,.38f,.25f,false);
                    if (random.nextBoolean()) add(x,y,PLANT,.76f,.70f,.12f,.15f,true);
                    break;
                case KITCHEN:
                    add(x,y,FRIDGE,.08f,.12f,.16f,.30f,true);
                    add(x,y,COUNTER,.75f,.12f,.16f,.42f,true);
                    add(x,y,TABLE,.33f,.60f,.32f,.18f,true);
                    if (random.nextBoolean()) add(x,y,CABINET,.42f,.12f,.23f,.10f,true);
                    break;
                case BEDROOM:
                    add(x,y,BED,.10f,.14f,.42f,.34f,true);
                    add(x,y,WARDROBE,.70f,.14f,.19f,.22f,true);
                    add(x,y,CABINET,.60f,.58f,.15f,.13f,true);
                    if (random.nextBoolean()) add(x,y,PLANT,.78f,.72f,.10f,.12f,true);
                    break;
                case BATHROOM:
                    add(x,y,BATH,.09f,.13f,.36f,.23f,true);
                    add(x,y,SINK,.70f,.14f,.16f,.16f,true);
                    add(x,y,TOILET,.69f,.54f,.14f,.17f,true);
                    break;
                case HALL:
                    add(x,y,RUG,.25f,.31f,.50f,.36f,false);
                    add(x,y,CABINET,.10f,.13f,.22f,.17f,true);
                    add(x,y,PLANT,.76f,.68f,.12f,.15f,true);
                    break;
                case STUDY:
                    add(x,y,DESK,.12f,.13f,.40f,.18f,true);
                    add(x,y,CHAIR,.26f,.38f,.13f,.13f,true);
                    add(x,y,SHELF,.70f,.13f,.16f,.34f,true);
                    add(x,y,PLANT,.75f,.70f,.12f,.14f,true);
                    break;
                case WORKSHOP:
                    add(x,y,WORKBENCH,.12f,.13f,.46f,.23f,true);
                    add(x,y,SHELF,.70f,.14f,.17f,.40f,true);
                    add(x,y,CABINET,.12f,.68f,.22f,.14f,true);
                    break;
                default:
                    add(x,y,GREENHOUSE,.12f,.12f,.32f,.25f,true);
                    add(x,y,PLANT,.67f,.16f,.13f,.16f,true);
                    add(x,y,PLANT,.68f,.60f,.14f,.16f,true);
                    add(x,y,TABLE,.26f,.64f,.30f,.13f,true);
                    break;
            }
        }
    }
    private void buildNavigationGrid() {
        walkable = new boolean[gridH][gridW];
        for (int gy=0;gy<gridH;gy++) for(int gx=0;gx<gridW;gx++) {
            float x=(gx+.5f)/gridW, y=gy==rows*GRID ? 1.025f : (gy+.5f)/(rows*GRID);
            walkable[gy][gx]=isWalkablePoint(x,y);
        }
        // Make the doorway throat explicit to avoid edge rounding closing the exit.
        int exitCell=(int)(.5f*gridW);
        walkable[rows*GRID][Math.max(0,exitCell-1)]=true;
        walkable[rows*GRID][Math.min(gridW-1,exitCell)]=true;
    }

    /** Keep every room's door approaches connected. If unusual furniture combinations seal a passage,
     * the generator removes the least essential solid object from that room and validates again. */
    private void ensureNavigationConnectivity() {
        int maxAttempts=Math.max(1,furniture.size()+1);
        for (int attempt=0; attempt<maxAttempts; attempt++) {
            buildNavigationGrid();
            int badRoom=-1;
            for (Room room:rooms) if(!roomIsConnected(room.x,room.y)) { badRoom=room.index(cols); break; }
            if(badRoom<0) return;
            Room room=rooms[badRoom]; int removeAt=-1;
            // Decorational plants, chairs and cabinets are sacrificed before large essential pieces.
            int[] preference={PLANT,CHAIR,CABINET,SHELF,WARDROBE,TOILET,SINK,FRIDGE,COUNTER,TABLE,DESK,WORKBENCH,SOFA,BED,BATH,TV,GREENHOUSE};
            outer: for(int type:preference) for(int i=furniture.size()-1;i>=0;i--){Furniture f=furniture.get(i);if(f.roomX==room.x&&f.roomY==room.y&&f.solid&&f.type==type){removeAt=i;break outer;}}
            if(removeAt<0) for(int i=furniture.size()-1;i>=0;i--){Furniture f=furniture.get(i);if(f.roomX==room.x&&f.roomY==room.y&&f.solid){removeAt=i;break;}}
            if(removeAt<0) return;
            furniture.remove(removeAt);
        }
        buildNavigationGrid();
    }
    private boolean roomIsConnected(int rx,int ry) {
        int x0=rx*GRID,y0=ry*GRID;
        boolean[][] seen=new boolean[GRID][GRID];int[] qx=new int[GRID*GRID],qy=new int[GRID*GRID];
        int sx=-1,sy=-1,best=Integer.MAX_VALUE;
        for(int y=0;y<GRID;y++)for(int x=0;x<GRID;x++){int score=(x*2-9)*(x*2-9)+(y*2-9)*(y*2-9);if(walkable[y0+y][x0+x]&&score<best){best=score;sx=x;sy=y;}}
        if(sx<0)return false;int head=0,tail=0;qx[tail]=sx;qy[tail++]=sy;seen[sy][sx]=true;int[] dx={1,-1,0,0},dy={0,0,1,-1};
        while(head<tail){int x=qx[head],y=qy[head++];for(int k=0;k<4;k++){int nx=x+dx[k],ny=y+dy[k];if(nx<0||nx>=GRID||ny<0||ny>=GRID||seen[ny][nx]||!walkable[y0+ny][x0+nx])continue;seen[ny][nx]=true;qx[tail]=nx;qy[tail++]=ny;}}
        if(rx>0&&!edgeReachable(seen,0,true))return false;
        if(rx<cols-1&&!edgeReachable(seen,GRID-1,true))return false;
        if(ry>0&&!edgeReachable(seen,0,false))return false;
        if(ry<rows-1&&!edgeReachable(seen,GRID-1,false))return false;
        return true;
    }
    private boolean edgeReachable(boolean[][] seen,int edge,boolean vertical) {
        for(int mid=GRID/2-1;mid<=GRID/2;mid++) {
            if(vertical){if(seen[mid][edge]||(edge==0&&seen[mid][1])||(edge==GRID-1&&seen[mid][GRID-2]))return true;}
            else {if(seen[edge][mid]||(edge==0&&seen[1][mid])||(edge==GRID-1&&seen[GRID-2][mid]))return true;}
        }
        return false;
    }

    private void placeFood() {
        food.clear();
        int wanted=Math.min(7, Math.max(3, cols+rows-1));
        for (int i=0;i<wanted;i++) {
            int roomIndex;
            if (i==0) roomIndex=findRoomType(KITCHEN);
            else roomIndex=random.nextInt(rooms.length);
            float[] spot=randomOpenSpot(roomIndex%cols, roomIndex/cols, .10f, .90f);
            food.add(new Food(spot[0],spot[1]));
        }
    }
    private int findRoomType(int type) { for (Room r:rooms) if(r.type==type) return r.index(cols); return 0; }
    /** Returns a route-grid cell centre, so AI/food targets always agree with pathfinding. */
    public float[] randomOpenSpot(int roomX,int roomY,float min,float max) {
        roomX=Math.max(0,Math.min(cols-1,roomX));roomY=Math.max(0,Math.min(rows-1,roomY));
        min=Math.max(0,Math.min(.95f,min));max=Math.max(min,Math.min(1f,max));
        for(int i=0;i<160;i++) {
            int gx=roomX*GRID+random.nextInt(GRID),gy=roomY*GRID+random.nextInt(GRID);
            float lx=((gx-roomX*GRID)+.5f)/GRID,ly=((gy-roomY*GRID)+.5f)/GRID;
            if(lx<min||lx>max||ly<min||ly>max||!walkable[gy][gx])continue;
            return new float[]{(gx+.5f)/gridW,(gy+.5f)/(rows*GRID)};
        }
        // The requested sub-area can be blocked by furniture; scan the preferred area first.
        int cx0=roomX*GRID,cy0=roomY*GRID;float best=Float.MAX_VALUE;int bestX=-1,bestY=-1;
        for(int gy=cy0;gy<cy0+GRID;gy++)for(int gx=cx0;gx<cx0+GRID;gx++)if(walkable[gy][gx]){
            float lx=((gx-cx0)+.5f)/GRID,ly=((gy-cy0)+.5f)/GRID;
            if(lx<min||lx>max||ly<min||ly>max)continue;
            float dx=lx-.5f,dy=ly-.5f,d=dx*dx+dy*dy;if(d<best){best=d;bestX=gx;bestY=gy;}
        }
        // If no cell survives the bounds, return any reachable cell in the room, never a blocked centre.
        if(bestX<0)for(int gy=cy0;gy<cy0+GRID;gy++)for(int gx=cx0;gx<cx0+GRID;gx++)if(walkable[gy][gx]){
            float dx=(gx-cx0+.5f)/GRID-.5f,dy=(gy-cy0+.5f)/GRID-.5f,d=dx*dx+dy*dy;
            if(d<best){best=d;bestX=gx;bestY=gy;}
        }
        if(bestX<0)return new float[]{(roomX+.5f)/cols,(roomY+.5f)/rows};
        return new float[]{(bestX+.5f)/gridW,(bestY+.5f)/(rows*GRID)};
    }
    public int roomAt(float x,float y) {
        int rx=Math.max(0,Math.min(cols-1,(int)Math.floor(x*cols)));
        int ry=Math.max(0,Math.min(rows-1,(int)Math.floor(y*rows)));
        return ry*cols+rx;
    }
    public boolean blocked(float x,float y,float margin) {
        if (x<.012f || x>.988f || y<.01f || y>1.08f) return true;
        if (y>1.0f && Math.abs(x-.5f)>.115f) return true;
        float xx=x*cols, yy=y*rows;
        int rx=Math.max(0,Math.min(cols-1,(int)Math.floor(xx)));
        int ry=Math.max(0,Math.min(rows-1,(int)Math.floor(yy)));
        float lx=xx-rx, ly=yy-ry;
        for(Furniture f:furniture) if(f.solid && f.roomX==rx && f.roomY==ry) {
            if(lx>=f.x-margin && lx<=f.x+f.w+margin && ly>=f.y-margin && ly<=f.y+f.h+margin) return true;
        }
        return false;
    }
    public boolean canMove(float x,float y,float nx,float ny) {
        if (nx<.015f || nx>.985f || ny<.008f || ny>1.07f) return false;
        if (ny>1.0f && Math.abs(nx-.5f)>.11f) return false;
        int oldCol=(int)Math.floor(x*cols), newCol=(int)Math.floor(nx*cols);
        int oldRow=(int)Math.floor(y*rows), newRow=(int)Math.floor(ny*rows);
        if (oldCol!=newCol && oldRow==newRow && oldCol>=0 && oldCol<cols && newCol>=0 && newCol<cols) {
            float localY=y*rows-oldRow;
            if(Math.abs(localY-.5f)>.10f) return false;
        }
        if (oldRow!=newRow && oldCol==newCol && oldRow>=0 && oldRow<rows && newRow>=0 && newRow<rows) {
            float localX=x*cols-oldCol;
            if(Math.abs(localX-.5f)>.10f) return false;
        }
        if (oldRow==rows-1 && newRow>=rows && Math.abs(nx-.5f)>.115f) return false;
        return !blocked(nx,ny,.030f);
    }
    private boolean isWalkablePoint(float x,float y) {
        if (y>1.0f) return Math.abs(x-.5f)<.105f;
        if (x<=.018f || x>=.982f || y<=.018f || (y>=.985f && Math.abs(x-.5f)>.095f)) return false;
        return !blocked(x,y,.045f);
    }
    private int gridX(float x) { return Math.max(0,Math.min(gridW-1,(int)(x*gridW))); }
    private int gridY(float y) { return Math.max(0,Math.min(gridH-1,(int)(y*rows*GRID))); }
    private boolean connected(int gx,int gy,int nx,int ny) {
        if(nx<0||nx>=gridW||ny<0||ny>=gridH||!walkable[ny][nx]) return false;
        if (ny==rows*GRID) return Math.abs((nx+.5f)/gridW-.5f)<.12f && gy==rows*GRID-1;
        if (gy==rows*GRID && ny<rows*GRID) return Math.abs((gx+.5f)/gridW-.5f)<.12f && ny==rows*GRID-1;
        if (gx/GRID != nx/GRID && gy==ny) {
            int roomY=gy/GRID; float localY=(gy%GRID+.5f)/GRID;
            if(Math.abs(localY-.5f)>.10f) return false;
        }
        if (gy/GRID != ny/GRID && gx==nx) {
            float localX=(gx%GRID+.5f)/GRID;
            if(Math.abs(localX-.5f)>.10f) return false;
        }
        return true;
    }
    /** Resolves a user-selected point to a nearby walkable point when the exact point is blocked. */
    public float[] resolveTarget(float targetX,float targetY) {
        if(targetY>=1f) return new float[]{.5f,1.025f};
        targetX=Math.max(.025f,Math.min(.975f,targetX)); targetY=Math.max(.025f,Math.min(.975f,targetY));
        int gx=gridX(targetX),gy=gridY(targetY);
        if(walkable[gy][gx])return new float[]{(gx+.5f)/gridW,(gy+.5f)/(rows*GRID)};
        int bestX=-1,bestY=-1;float best=Float.MAX_VALUE;
        for(int radius=1;radius<Math.max(gridW,gridH);radius++) {
            for(int yy=Math.max(0,gy-radius);yy<=Math.min(gridH-1,gy+radius);yy++)for(int xx=Math.max(0,gx-radius);xx<=Math.min(gridW-1,gx+radius);xx++){
                if(Math.max(Math.abs(xx-gx),Math.abs(yy-gy))!=radius||!walkable[yy][xx])continue;
                float d=(xx-gx)*(xx-gx)+(yy-gy)*(yy-gy);if(d<best){best=d;bestX=xx;bestY=yy;}
            }
            if(bestX>=0)break;
        }
        if(bestX<0)return new float[]{.5f,.5f};
        return new float[]{(bestX+.5f)/gridW,bestY==rows*GRID?1.025f:(bestY+.5f)/(rows*GRID)};
    }
    /** Returns a reachable waypoint along a deterministic grid-BFS route, or the target in its cell. */
    public float[] nextWaypoint(float x,float y,float targetX,float targetY) {
        if(targetY>=1f) targetY=1.025f;
        else {float[] resolved=resolveTarget(targetX,targetY);targetX=resolved[0];targetY=resolved[1];}
        targetX=Math.max(.025f,Math.min(.975f,targetX)); targetY=Math.max(.025f,Math.min(1.025f,targetY));
        int sx=gridX(x), sy=gridY(y), gx=gridX(targetX), gy=gridY(targetY);
        if(sx==gx&&sy==gy) return new float[]{targetX,targetY};
        int[][] prev=new int[gridH][gridW]; for(int[] row:prev) Arrays.fill(row,-1);
        int size=gridW*gridH; int[] qx=new int[size],qy=new int[size]; int head=0,tail=0;
        qx[tail]=sx; qy[tail++]=sy; prev[sy][sx]=sy*gridW+sx;
        int[] dx={1,-1,0,0},dy={0,0,1,-1};
        while(head<tail){int cx=qx[head],cy=qy[head++];if(cx==gx&&cy==gy)break;
            for(int k=0;k<4;k++){int nx=cx+dx[k],ny=cy+dy[k];if(nx<0||nx>=gridW||ny<0||ny>=gridH||prev[ny][nx]>=0||!connected(cx,cy,nx,ny))continue;prev[ny][nx]=cy*gridW+cx;qx[tail]=nx;qy[tail++]=ny;}
        }
        if(prev[gy][gx]<0) {
            // Defensive fallback: choose the closest cell actually reachable from the fly.
            // Never send the agent straight through a wall when a malformed target is supplied.
            int bestX=sx,bestY=sy;float best=Float.MAX_VALUE;
            for(int yy=0;yy<gridH;yy++)for(int xx=0;xx<gridW;xx++)if(prev[yy][xx]>=0){
                float dtx=xx-gx,dty=yy-gy,d=dtx*dtx+dty*dty;if(d<best){best=d;bestX=xx;bestY=yy;}
            }
            return new float[]{(bestX+.5f)/gridW,bestY==rows*GRID?1.025f:(bestY+.5f)/(rows*GRID)};
        }
        int cx=gx,cy=gy; while(prev[cy][cx] != sy*gridW+sx) {int code=prev[cy][cx]; if(code<0)break; cx=code%gridW;cy=code/gridW;}
        float wx=(cx+.5f)/gridW, wy=cy==rows*GRID?1.025f:(cy+.5f)/(rows*GRID);
        return new float[]{wx,wy};
    }
    public int furnitureCount(){return furniture.size();}
    public int activeFoodCount(){int n=0;for(Food f:food)if(f.active)n++;return n;}
    /** Stable layout signature useful for tests and bug reports. */
    public String signature(){StringBuilder b=new StringBuilder();b.append(seed).append(':').append(cols).append('x').append(rows).append(':');for(Room r:rooms)b.append(r.type).append(',');b.append('|');for(Furniture f:furniture)b.append(f.roomX).append('/').append(f.roomY).append('/').append(f.type).append('/').append(Float.floatToIntBits(f.x)).append('/').append(Float.floatToIntBits(f.y)).append('/').append(Float.floatToIntBits(f.w)).append('/').append(Float.floatToIntBits(f.h)).append(',');b.append('|');for(Food f:food)b.append(Float.floatToIntBits(f.x)).append('/').append(Float.floatToIntBits(f.y)).append(',');return b.toString();}
}
