package apertx.teddy;
import android.app.*;
import android.opengl.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import java.util.*;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import static android.opengl.GLES10.*;

public class TeddyActivity extends Activity implements GLSurfaceView.Renderer,OnTouchListener{
 protected void onCreate(Bundle b){
  super.onCreate(b);
  glsv=new GLSurfaceView(this);
  glsv.setRenderer(this);
  glsv.setOnTouchListener(this);
  setContentView(glsv);
  ui=new UI(this,R.drawable.dpad);
  teddys=new ArrayList<Teddy>();
  teddys.add(new Teddy(this,R.raw.mirai));
  teddys.add(new Teddy(this,R.raw.mirac));
  teddys.add(new Teddy(this,R.raw.mirau));
  teddys.add(new Teddy(this,R.raw.miku));
  teddys.get(1).move(4,0,0);
  teddys.get(2).move(0,0,-8);
  teddys.get(3).move(-4,0,-4);
  teddys.get(2).scale(2.0f);
  teddys.get(2).setHitbox(true);
  size=20.0f;
  camera=new Camera(size);
  camera.setTeddys(teddys);
  camera.setGrid(true);
  rPointer=-1;
  mPointer=-1;
  rPointerId=-1;
  mPointerId=-1;
 }

 public void onSurfaceCreated(GL10 gl,EGLConfig ec){
  glDisable(GL_DITHER);
  glEnable(GL_DEPTH_TEST);
  glHint(GL_PERSPECTIVE_CORRECTION_HINT,GL_FASTEST);
  glShadeModel(GL_SMOOTH);
  glEnableClientState(GL_VERTEX_ARRAY);
  glEnableClientState(GL_NORMAL_ARRAY);
  glClearColor(0.125f,0.125f,0.125f,1.0f);
  glMatrixMode(GL_MODELVIEW);

  glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
  glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
  glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
  glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
  glTexEnvf(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_REPLACE);
  glActiveTexture(GL_TEXTURE0);
  glEnable(GL_TEXTURE_2D);
  glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
  glEnable(GL_BLEND);

  glLightfv(GL_LIGHT0,GL_POSITION,new float[]{size,size,size,0},0);
  glEnable(GL_LIGHTING);
  glEnable(GL_LIGHT0);
  glEnable(GL_COLOR_MATERIAL);
  glEnable(GL_CULL_FACE);
  glFogf(GL_FOG_START,2*size*0.75f);
  glFogf(GL_FOG_END,2*size*0.95f);
  glFogfv(GL_FOG_COLOR,new float[]{0.125f,0.125f,0.125f,0.0f},0);
  glFogx(GL_FOG_MODE,GL_LINEAR);
  glEnable(GL_FOG);
  glLineWidth(4.0f);
 }
 public void onSurfaceChanged(GL10 gl,int w,int h){
  glViewport(0,0,w,h);
  ui.resize(w,h);
  sw=w;
  sh=h;
  float rw=1.0f;
  float rh=1.0f;
  if(w>h)rw=(float)w/h;
  else rh=(float)h/w;
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  glFrustumf(-rw,rw,-rh,rh,1.0f,size*2-1);
  glMatrixMode(GL_MODELVIEW);
 }

 public void onDrawFrame(GL10 gl){
  glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
  camera.move(mx,my);
  camera.look(rx,ry);
  glLoadIdentity();
  camera.update(gl);
  for(Teddy teddy:teddys)teddy.draw();
  ui.draw(gl);
 }

 public boolean onTouch(View v,MotionEvent e){
  int action=e.getAction();
  switch(action&e.ACTION_MASK){
   case e.ACTION_DOWN:
   case e.ACTION_POINTER_DOWN:
    int tapIndex=(action&e.ACTION_POINTER_ID_MASK)>>e.ACTION_POINTER_ID_SHIFT;
    int tapId=e.getPointerId(tapIndex);
    if(e.getX(tapIndex)<sw/2){
     if(mPointer==-1){
      mPointer=tapIndex;
      if(rPointer!=-1)rPointer=mPointer==0?1:0;
      mTapX=e.getX(mPointer);
      mTapY=e.getY(mPointer);
      mPointerId=tapId;
     }
    }else if(rPointer==-1){
     rPointer=tapIndex;
     if(mPointer!=-1)mPointer=rPointer==0?1:0;
     rTapX=e.getX(rPointer);
     rTapY=e.getY(rPointer);
     rPointerId=tapId;
    }
    break;
   case e.ACTION_MOVE:
    if(rPointer!=-1&&(e.getX(rPointer)!=rTapX|e.getY(rPointer)!=rTapY)){
     rx+=(rTapX-e.getX(rPointer))/256;
     ry+=(rTapY-e.getY(rPointer))/256;
     if(ry>1.5f)ry=1.5f;
     else if(ry<-1.5f)ry=-1.5f;
     rTapX=e.getX(rPointer);
     rTapY=e.getY(rPointer);
    }
    if(mPointer!=-1&&(e.getX(mPointer)!=mTapX|e.getY(mPointer)!=mTapY)){
     mx=(mTapX-e.getX(mPointer))/256;
     my=(mTapY-e.getY(mPointer))/256;
     if(mx>0.5f)mx=0.5f;
     else if(mx<-0.5f)mx=-0.5f;
     if(my>0.5f)my=0.5f;
     else if(my<-0.5f)my=-0.5f;
    }
    break;
   case e.ACTION_POINTER_UP:
    int utapIndex=(action&e.ACTION_POINTER_ID_MASK)>>e.ACTION_POINTER_ID_SHIFT;
    int utapId=e.getPointerId(utapIndex);
    if(rPointerId==utapId){
     mPointer=0;
     rPointer=-1;
    }
    if(mPointerId==utapId){
     rPointer=0;
     mPointer=-1;
     mx=0.0f;
     my=0.0f;
    }
    break;
   case e.ACTION_CANCEL:
   case e.ACTION_UP:
    rPointer=-1;
    mPointer=-1;
    mPointerId=-1;
    rPointerId=-1;
    mx=0.0f;
    my=0.0f;
    break;
  }
  return true;
 }

 protected void onResume(){
  super.onResume();
  glsv.onResume();
 }
 protected void onPause(){
  super.onPause();
  glsv.onPause();
 }

 private GLSurfaceView glsv;
 private UI ui;
 private Camera camera;
 private List<Teddy>teddys;
 private int sw;
 private int sh;
 private float rTapX;
 private float rTapY;
 private float rx;
 private float ry;
 private float mTapX;
 private float mTapY;
 private float mx;
 private float my;
 private int rPointer;
 private int mPointer;
 private int rPointerId;
 private int mPointerId;
 private float size;
}
