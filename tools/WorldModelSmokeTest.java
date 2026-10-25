import com.flylife.game.WorldModel;
import java.util.Random;

/** Standalone deterministic-world and navigation regression test; no Android SDK is required. */
public final class WorldModelSmokeTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static boolean canReach(WorldModel world, float x, float y, float tx, float ty, int limit) {
        for (int i=0;i<limit;i++) {
            if (Math.hypot(tx-x,ty-y)<.025) return true;
            float[] waypoint=world.nextWaypoint(x,y,tx,ty);
            float dx=waypoint[0]-x,dy=waypoint[1]-y,d=(float)Math.hypot(dx,dy);
            if (d<1e-6) return Math.hypot(tx-x,ty-y)<.025 || (ty>1f&&y>=1f&&Math.abs(x-.5f)<.13f);
            float step=Math.min(.003f,d),nx=x+dx/d*step,ny=y+dy/d*step;
            if(world.canMove(x,y,nx,ny)){x=nx;y=ny;}
            else if(world.canMove(x,y,nx,y))x=nx;
            else if(world.canMove(x,y,x,ny))y=ny;
            else return false;
            if(ty>1f&&y>=1f&&Math.abs(x-.5f)<.13f)return true;
        }
        return Math.hypot(tx-x,ty-y)<.05;
    }

    public static void main(String[] args) {
        long[] edgeSeeds={0,1,-1,123456789L,0x46f19a31L,Long.MAX_VALUE,Long.MIN_VALUE};
        for(long seed:edgeSeeds) {
            WorldModel a=WorldModel.generate(seed),b=WorldModel.generate(seed);
            check(a.signature().equals(b.signature()),"same seed differs: "+seed);
            check(a.rooms.length==a.cols*a.rows,"room count mismatch");
            check(a.furnitureCount()>=a.rooms.length,"house unexpectedly empty");
            check(a.activeFoodCount()>=3,"food missing");
            boolean kitchen=false;
            for(WorldModel.Room r:a.rooms) {
                if(r.type==WorldModel.KITCHEN) kitchen=true;
                check(r.type>=WorldModel.LIVING&&r.type<=WorldModel.CONSERVATORY,"invalid room type");
            }
            check(kitchen,"no kitchen in seed "+seed);
            for(WorldModel.Food f:a.food)if(f.active)check(!a.blocked(f.x,f.y,.03f),"food intersects furniture seed="+seed);
            for(WorldModel.Furniture f:a.furniture) if(f.solid){
                float cx=(f.roomX+f.x+f.w*.5f)/a.cols,cy=(f.roomY+f.y+f.h*.5f)/a.rows;
                check(a.blocked(cx,cy,.01f),"solid furniture collision not registered seed="+seed);
            }
            Random taps=new Random(seed^0x71C3A5L);
            for(int tap=0;tap<8;tap++){
                float[] goal=a.resolveTarget(.04f+taps.nextFloat()*.92f,.04f+taps.nextFloat()*.92f);
                check(goal[0]>=0&&goal[0]<=1&&goal[1]>=0&&goal[1]<=1,"resolved target out of bounds seed="+seed);
                check(!a.blocked(goal[0],goal[1],.01f),"resolved tap target still blocked seed="+seed);
            }
            float[] start=a.randomOpenSpot(0,0,.42f,.58f);
            check(canReach(a,start[0],start[1],.5f,1.025f,20000),"exit unreachable seed="+seed);
        }
        check(!WorldModel.generate(123L).signature().equals(WorldModel.generate(124L).signature()),"different seeds produce identical worlds");

        // Randomized cross-room paths guard against furniture layouts that seal a doorway or aisle.
        int routeCount=0;
        for(int s=0;s<200;s++) {
            long seed=s*1009L-44; WorldModel w=WorldModel.generate(seed); Random r=new Random(s*17L+3);
            for(int q=0;q<3;q++) {
                int startRoom=r.nextInt(w.rooms.length),goalRoom=(startRoom+1+r.nextInt(w.rooms.length-1))%w.rooms.length;
                WorldModel.Room sr=w.rooms[startRoom],gr=w.rooms[goalRoom];
                float[] from=w.randomOpenSpot(sr.x,sr.y,.35f,.65f),to=w.randomOpenSpot(gr.x,gr.y,.35f,.65f);
                check(canReach(w,from[0],from[1],to[0],to[1],12000),"route failed seed="+seed+" "+startRoom+" -> "+goalRoom);
                routeCount++;
            }
            for(int i=0;i<w.furniture.size();i++)for(int j=i+1;j<w.furniture.size();j++) {
                WorldModel.Furniture a=w.furniture.get(i),b=w.furniture.get(j);
                if(a.roomX!=b.roomX||a.roomY!=b.roomY||!a.solid||!b.solid)continue;
                boolean overlap=a.x+.005f<b.x+b.w&&b.x+.005f<a.x+a.w&&a.y+.005f<b.y+b.h&&b.y+.005f<a.y+a.h;
                check(!overlap,"solid furniture overlaps seed="+seed+" type="+a.type+"/"+b.type);
            }
        }
        System.out.println("FlyLife procedural-world smoke tests passed: edge seeds, deterministic generation, food placement, collision bounds, furniture overlap, resolved tap targets and "+routeCount+" navigable routes.");
    }
}
