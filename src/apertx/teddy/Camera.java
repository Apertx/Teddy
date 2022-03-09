package apertx.teddy;
import android.opengl.*;
import java.util.*;
import javax.microedition.khronos.opengles.*;
import java.nio.*;

import static android.opengl.GLES10.*;

public class Camera{
 public Camera(float size){
  this.size=size;
  eyeX=0.0f;
  eyeY=1.0f;
  eyeZ=size;
  normX=0.0f;
  normY=0.0f;
  normZ=-1.0f;
  center();

  ByteBuffer vbb=ByteBuffer.allocateDirect((int)size*96+24);
  vbb.order(ByteOrder.nativeOrder());
  bGrid=vbb.asFloatBuffer();
  bGrid.put(size).put(0).put(0).
   put(-size).put(0).put(0);
  bGrid.put(0).put(0).put(size).
   put(0).put(0).put(-size);
  for(int i=1;i<size;i++){
   bGrid.put(size).put(0).put(i).
    put(-size).put(0).put(i);
   bGrid.put(size).put(0).put(-i).
    put(-size).put(0).put(-i);
   bGrid.put(i).put(0).put(size).
    put(i).put(0).put(-size);
   bGrid.put(-i).put(0).put(size).
    put(-i).put(0).put(-size);
  }
  bGrid.position(0);
 }

 public void update(GL10 gl){
  GLU.gluLookAt(gl,eyeX,eyeY,eyeZ,centerX,centerY,centerZ,0.0f,1.0f,0.0f);
  if(grid){
   glVertexPointer(3,GL_FLOAT,0,bGrid);
   glDisable(GL_LIGHTING);
   glDisableClientState(GL_NORMAL_ARRAY);
   glColor4f(0.5f,0.25f,0.25f,1.0f);
   glDrawArrays(GL_LINES,0,4);
   glColor4f(0.25f,0.25f,0.25f,1.0f);
   glDrawArrays(GL_LINES,4,(int)size*8);
   glEnableClientState(GL_NORMAL_ARRAY);
   glEnable(GL_LIGHTING);
  }
 }
 public void look(float art,float ary){
  if(art!=lart|ary!=lary){
   art+=(float)Math.PI;
   float cosy=(float)Math.cos(ary);
   normX=(float)Math.sin(art)*cosy;
   normY=(float)Math.sin(ary);
   normZ=(float)Math.cos(art)*cosy;
   center();
   lart=art;
   lary=ary;
  }
 }
 public void move(float disy,float dist){
  if(disy!=0.0f|dist!=0.0f){
   float leyeX=eyeX;
   float leyeY=eyeY;
   float leyeZ=eyeZ;
   eyeX+=dist*normX;
   eyeY+=dist*normY;
   eyeZ+=dist*normZ;
   eyeX+=disy*normZ;
   eyeZ-=disy*normX;
   boolean noOver=true;
   if(teddys!=null)for(Teddy teddy:teddys)if(teddy.contains(getPos())){
     noOver=false;
     break;
    }
   if(noOver){
    if(eyeX>size)eyeX=size;
    else if(eyeX<-size)eyeX=-size;
    if(eyeY>size)eyeY=size;
    else if(eyeY<-size)eyeY=-size;
    if(eyeZ>size)eyeZ=size;
    else if(eyeZ<-size)eyeZ=-size;
    center();
   }else{
    eyeX=leyeX;
    eyeY=leyeY;
    eyeZ=leyeZ;
   }
  }
 }
 public void moveTo(float x,float y,float z){
  eyeX=x;
  eyeY=y;
  eyeZ=z;
  center();
 }
 public void setTeddys(List<Teddy>teddys){
  this.teddys=teddys;
 }
 public float[]getPos(){
  return new float[]{eyeX,eyeY,eyeZ};
 }
 public void setGrid(boolean grid){
  this.grid=grid;
 }

 private void center(){
  centerX=eyeX+normX;
  centerY=eyeY+normY;
  centerZ=eyeZ+normZ;
 }
 private float eyeX;
 private float eyeY;
 private float eyeZ;
 private float centerX;
 private float centerY;
 private float centerZ;
 private float normX;
 private float normY;
 private float normZ;
 private float lart;
 private float lary;
 private List<Teddy>teddys;
 private float size;
 private boolean grid;
 private FloatBuffer bGrid;
}
