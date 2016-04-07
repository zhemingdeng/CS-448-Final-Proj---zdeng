package edu.sfsu.cs.orange.ocr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Queue;
//import org.jgrapht.*;

import com.googlecode.leptonica.android.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
//import org.junit.Test;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;

/**
 * Created by MICHAEL DENG on 3/27/2016.
 */
public class NavigationActivity extends Activity {
    private Button mRe_routeButton;
    private TextView mStartPointTextView;
    private EditText mEditText_Destination;
    private Button mNavigationButton;
    //mapView mMapView;
    private static int[][] brownMap;
    Graph g;
    Canvas canvas;
    Paint paint;
    Bitmap bitmap;
    Bitmap tempBitmap;
    private ImageView mImageView;
    private static String startPoint;
    private static String destPoint;
    private static int start_point_Int;
    private static int dest_point_Int;
    private static int path_start;
    private static int path_dest;
    Map<String,Integer> roomNumber=new HashMap<String, Integer>();
    Map<Integer,PointF> routePoint=new HashMap<Integer, PointF>();
    private static final String START_POINT="edu.sfsu.cs.orange.ocr.start_point";

    public class mapView extends ImageView{
        Paint mPaint;
        public mapView(Context context){
            super(context);
            mPaint=new Paint();
        }
        public mapView(Context context,AttributeSet attributeSet){
            super(context, attributeSet);
        }
        public mapView(Context context,AttributeSet attributeSet,int defStyle){
            super(context, attributeSet, defStyle);
        }
        @Override
        public void onDraw(Canvas canvas){
            super.onDraw(canvas);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mRe_routeButton=(Button)findViewById(R.id.re_route_button);
        mRe_routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //back to welcome Activity
            }
        });
        startPoint=getIntent().getStringExtra(START_POINT);
        mStartPointTextView=(TextView)findViewById(R.id.startpoint_textview);
        mStartPointTextView.setText("Start Point is: " + startPoint);
        mEditText_Destination=(EditText)findViewById(R.id.destination_edittext_view);
        mEditText_Destination.setEnabled(true);



        mImageView=(ImageView)findViewById(R.id.image_view);
        Drawable drawable=this.getResources().getDrawable(R.drawable.brown_280_floor_plan);
        bitmap=((BitmapDrawable)drawable).getBitmap();
        tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        canvas=new Canvas(tempBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint=new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10.0f);
        //canvas.drawLine(1.0f,100.0f,101.0f,10.0f,paint); //draw a sample line
        mImageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));

        //read office digit number, office room number in string and office x,y coordinate from txt file
        //split[0]: office digit number
        //split[1]: office roomNum in string
        //split[2]: office x coordinate in float
        //split[3]: office y coordinate in float
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("officeLocation.txt"),"UTF-8"))) {
            String line;
            String[] split;

            while ((line = br.readLine()) != null) {
                // process the line.
                split=line.split("\\s+");
                roomNumber.put(split[1],Integer.parseInt(split[0]));
                PointF point=new PointF();
                point.x=Float.parseFloat(split[2]);
                point.y=Float.parseFloat(split[3]);
                routePoint.put(Integer.parseInt(split[0]),point);
                paint.setColor(Color.BLUE);
                room_loc_draw(canvas, paint, point.x, point.y); //draw circles on the map as office location
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }


        //read the office relationship from txt file
        //split[0] and split[1]: office digit number in roomNum map and routePoint map.
        //split[2]: the weight/distance between two offices/intersection
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("vertexRelationship.txt"),"UTF-8"))) {
            String line;
            String[] split;
            if((line=br.readLine())!=null){
                split=line.split("\\s");
                brownMap=new int[Integer.parseInt(split[0])][Integer.parseInt(split[1])]; //initialize the brownMap array
            }
            Log.i("TAG",line);
            while ((line = br.readLine()) != null) {
                // process the line.

                split=line.split("\\s");
                brownMap[Integer.parseInt(split[0])][Integer.parseInt(split[1])]=Integer.parseInt(split[2]);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        g=new Graph(brownMap);
        mNavigationButton=(Button)findViewById(R.id.navigation_button);
        mNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destPoint=mEditText_Destination.getText().toString();
                if(roomNumber.get(startPoint)!=null){
                    start_point_Int=roomNumber.get(startPoint);
                }
                if(roomNumber.get(destPoint)!=null){
                    dest_point_Int=roomNumber.get(destPoint);
                }
                int[] prev=DijKstra(start_point_Int,dest_point_Int,g); //calculate the shortest route and save it in a array
                Stack<Integer> route=traceBack(prev, dest_point_Int); //store the route point into a stack
                /*if(!route.isEmpty()) {//pop out route point one by one
                    path_start=route.pop();
                }*/
                path_start=start_point_Int;
                paint.setColor(Color.RED);
                while(!route.isEmpty()){
                    path_dest=route.pop();
                    if(routePoint.get(path_start)!=null&&routePoint.get(path_dest)!=null){
                        PointF start=routePoint.get(path_start);
                        PointF dest=routePoint.get(path_dest);
                        canvas.drawLine(start.x,start.y,dest.x,dest.y,paint);
                    }
                    path_start=path_dest;
                }
                //canvas.drawLine(100.0f,500.0f,501.0f,100f,paint);
                mImageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
            }
        });
    }
    //get start_point from WelcomeActivity or CaptureActivity
    public static Intent newIntent(Context packageContext, String startPoint){
        Intent i=new Intent(packageContext,NavigationActivity.class);
        i.putExtra(START_POINT,startPoint);
        return i;
    }

    //useless
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.activity_navigation, menu);

        return true;
    }
    /*
    private void addLane(String laneId, int sourceLocNo, int destLocNo,int duration) {
        Edge lane = new Edge(laneId,nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
        edges.add(lane);
    }*/
    /**
     *
     * push route point, from destination point
     * @param prev trace back history int[]
     * @param target destination
     * @return
     */
    public Stack<Integer> traceBack(int[] prev, int target){
        Stack<Integer> s=new Stack<Integer>();
        int u=target;
        while(prev[u]!=start_point_Int){
            s.push(u);
            u=prev[u];
        }
        return s;
    }

    //DijKstra algorithm, find the shortest path
    public int[] DijKstra(int start,int dest,Graph g){
        Queue<Integer> q=new LinkedList<Integer>();
        int[] dist=new int[g.getSize()];
        int[] prev=new int[g.getSize()];
        boolean[] visited=new boolean[g.getSize()];
        int alt=0;
        for(int i=0;i<g.getSize();i++){
            dist[i]=Integer.MAX_VALUE;
        }
        int source=start; //the first node in queue
        dist[source]=0;
        q.add(source);
        while(!q.isEmpty()){
            int u=q.remove();
            visited[u]=true;
            if(u==dest){
                break;
            }
            for(int i=0;i<g.getSize();i++){
                if(g.adjNodes(u, i)){
                    if(visited[i]!=true){
                        q.add(i);
                        alt=dist[u]+g.getWeight(u, i);
                        if(alt<dist[i]){
                            dist[i]=alt;
                            prev[i]=u;
                        }
                    }

                }
            }
        }
        return prev;
    }
    private void room_loc_draw(Canvas canvas,Paint paint,float x,float y){
        canvas.drawCircle(x,y,50f,paint);
    }


    /*<ImageView
    android:id="@+id/image_view"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/floorplan"/>
    */
}
