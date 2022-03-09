package apertx.teddy;
import android.content.*;
import android.opengl.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import javax.microedition.khronos.opengles.*;

import static android.opengl.GLES10.*;
import android.graphics.*;

public class Teddy{
 public Teddy(Context c,int res){
  List<Float>lVert=new ArrayList<Float>();
  List<Integer>lInd=new ArrayList<Integer>();
  List<Float>lCol=new ArrayList<Float>();

  InputStream is=c.getResources().openRawResource(res);
  String str="";
  try{
   byte[]buf=new byte[is.available()];
   is.read(buf);
   str=new String(buf);
  }catch(Exception e){}
  String[]lines=str.split("\n");
  str=null;
  for(int i=0;i<lines.length;i++)
   if(lines[i].startsWith("f ")){
    boolean slash=lines[i].contains("/");
    String[]dots=lines[i].trim().split(" ");
    if(dots.length==4)for(int j=1;j<4;j++)lInd.add(Integer.parseInt(slash?dots[j].substring(0,dots[j].indexOf('/')):dots[j])-1);
    else if(dots.length==5){
     lInd.add(Integer.parseInt(slash?dots[1].substring(0,dots[1].indexOf('/')):dots[1])-1);
     lInd.add(Integer.parseInt(slash?dots[2].substring(0,dots[2].indexOf('/')):dots[2])-1);
     lInd.add(Integer.parseInt(slash?dots[3].substring(0,dots[3].indexOf('/')):dots[3])-1);
     lInd.add(Integer.parseInt(slash?dots[1].substring(0,dots[1].indexOf('/')):dots[1])-1);
     lInd.add(Integer.parseInt(slash?dots[3].substring(0,dots[3].indexOf('/')):dots[3])-1);
     lInd.add(Integer.parseInt(slash?dots[4].substring(0,dots[4].indexOf('/')):dots[4])-1);
    }
   }else if(lines[i].startsWith("v ")){
    String[]dots=lines[i].trim().split(" ");
    float[]dot=new float[3];
    if(dots.length==4)
     for(int j=1;j<4;j++){
      float ff=Float.parseFloat(dots[j]);
      lVert.add(ff);
      dot[j-1]=ff;
      if(colored){
       lCol.add(0.0f);
       lCol.add(0.0f);
       lCol.add(0.0f);
       lCol.add(1.0f);
      }
     }
    else if(dots.length==7){
     colored=true;
     for(int j=1;j<4;j++)lVert.add(Float.parseFloat(dots[j]));
     for(int j=4;j<7;j++)lCol.add(Float.parseFloat(dots[j]));
     lCol.add(1.0f);
    }
    if(dot[0]>upX)upX=dot[0];
    if(dot[1]>upY)upY=dot[1];
    if(dot[2]>upZ)upZ=dot[2];
    if(dot[0]<lowX)lowX=dot[0];
    if(dot[1]<lowY)lowY=dot[1];
    if(dot[2]<lowZ)lowZ=dot[2];
   }

  ByteBuffer vbb=ByteBuffer.allocateDirect(lVert.size()*12);
  vbb.order(ByteOrder.nativeOrder());
  mFVertexBuffer=vbb.asFloatBuffer();
  ByteBuffer nbb=ByteBuffer.allocateDirect(lVert.size()*12);
  nbb.order(ByteOrder.nativeOrder());
  mFNormalBuffer=nbb.asFloatBuffer();
  ByteBuffer ibb=ByteBuffer.allocateDirect(lInd.size()*6);
  ibb.order(ByteOrder.nativeOrder());
  mIndexBuffer=ibb.asShortBuffer();
  ByteBuffer bvb=ByteBuffer.allocateDirect(8*12);
  bvb.order(ByteOrder.nativeOrder());
  boxVert=bvb.asFloatBuffer();
  boxVert.
   put(upX).put(upY).put(upZ).
   put(upX).put(upY).put(lowZ).
   put(lowX).put(upY).put(upZ).
   put(lowX).put(upY).put(lowZ).
   put(upX).put(lowY).put(upZ).
   put(upX).put(lowY).put(lowZ).
   put(lowX).put(lowY).put(upZ).
   put(lowX).put(lowY).put(lowZ);
  ByteBuffer bib=ByteBuffer.allocateDirect(12*4);
  bib.order(ByteOrder.nativeOrder());
  boxIndex=bib.asShortBuffer();
  boxIndex.put((short)0).put((short)1).
   put((short)0).put((short)2).
   put((short)3).put((short)1).
   put((short)3).put((short)2).
   put((short)4).put((short)5).
   put((short)4).put((short)6).
   put((short)7).put((short)5).
   put((short)7).put((short)6).
   put((short)0).put((short)4).
   put((short)1).put((short)5).
   put((short)2).put((short)6).
   put((short)3).put((short)7);
  scale=1.0f;
  moved();
  if(colored){
   ByteBuffer cbb=ByteBuffer.allocateDirect(lVert.size()*12);
   cbb.order(ByteOrder.nativeOrder());
   mFColorBuffer=cbb.asFloatBuffer();
  }

  float[]lNorm=new float[lVert.size()];
  byte[]cNorm=new byte[lNorm.length];
  for(int i=0;i<lInd.size();i+=3){
   int av=lInd.get(i+0);
   int bv=lInd.get(i+1);
   int cv=lInd.get(i+2);
   mIndexBuffer.put((short)av).put((short)bv).put((short)cv);
   av*=3;
   bv*=3;
   cv*=3;
   float[]an=getNormal(
    lVert.get(av+0),lVert.get(bv+0),lVert.get(cv+0),
    lVert.get(av+1),lVert.get(bv+1),lVert.get(cv+1),
    lVert.get(av+2),lVert.get(bv+2),lVert.get(cv+2));
   lNorm[av+0]+=an[0];
   lNorm[av+1]+=an[1];
   lNorm[av+2]+=an[2];
   lNorm[bv+0]+=an[0];
   lNorm[bv+1]+=an[1];
   lNorm[bv+2]+=an[2];
   lNorm[cv+0]+=an[0];
   lNorm[cv+1]+=an[1];
   lNorm[cv+2]+=an[2];
   cNorm[av+0]++;
   cNorm[av+1]++;
   cNorm[av+2]++;
   cNorm[bv+0]++;
   cNorm[bv+1]++;
   cNorm[bv+2]++;
   cNorm[cv+0]++;
   cNorm[cv+1]++;
   cNorm[cv+2]++;
  }
  for(int i=0;i<lVert.size();i++){
   mFVertexBuffer.put(lVert.get(i));
   lNorm[i]/=cNorm[i];
   if(colored)mFColorBuffer.put(lCol.get(i));
  }

  verts=lInd.size();
  mFNormalBuffer.put(lNorm);
  mFVertexBuffer.position(0);
  mFNormalBuffer.position(0);
  mIndexBuffer.position(0);
  if(colored)mFColorBuffer.position(0);
  boxVert.position(0);
  boxIndex.position(0);
 }

 public void draw(){
  glVertexPointer(3,GL_FLOAT,0,mFVertexBuffer);
  glNormalPointer(GL_FLOAT,0,mFNormalBuffer);
  glPushMatrix();
  glTranslatef(tx,ty,tz);
  glScalef(scale,scale,scale);
  if(colored){
   glEnableClientState(GL_COLOR_ARRAY);
   glColorPointer(4,GL_FLOAT,0,mFColorBuffer);
  }else glColor4f(1,1,1,1);
  glDrawElements(GL_TRIANGLES,verts,GL_UNSIGNED_SHORT,mIndexBuffer);
  if(colored)glDisableClientState(GL_COLOR_ARRAY);
  if(hitbox){
   glDisable(GL_LIGHTING);
   glDisableClientState(GL_NORMAL_ARRAY);
   glVertexPointer(3,GL_FLOAT,0,boxVert);
   glColor4f(0.25f,0.5f,0.25f,1.0f);
   glDrawElements(GL_LINES,24,GL_UNSIGNED_SHORT,boxIndex);
   glEnableClientState(GL_NORMAL_ARRAY);
   glEnable(GL_LIGHTING);
  }
  glPopMatrix();
 }

 private float[]getNormal(float ax,float bx,float cx,float ay,float by,float cy,float az,float bz,float cz){
  float[]res=new float[3];
  float ux=bx-ax;
  float uy=by-ay;
  float uz=bz-az;
  float vx=cx-ax;
  float vy=cy-ay;
  float vz=cz-az;
  res[0]=uy*vz-uz*vy;
  res[1]=uz*vx-ux*vz;
  res[2]=ux*vy-uy*vx;
  float wrki=(float)Math.
   sqrt(res[0]*res[0]+
        res[1]*res[1]+
        res[2]*res[2]);
  res[0]/=wrki;
  res[1]/=wrki;
  res[2]/=wrki;
  return res;
 }

 public void move(float tx,float ty,float tz){
  this.tx=tx;
  this.ty=ty;
  this.tz=tz;
  moved();
 }
 public void scale(float scale){
  this.scale=scale;
  moved();
 }
 public float size(){
  return upY;
 }
 public boolean contains(float[]p){
  return p[0]<cupX&&p[0]>clowX&&
   p[1]<cupY&&p[1]>clowY&&
   p[2]<cupZ&&p[2]>clowZ;
 }
 public void setHitbox(boolean hitbox){
  this.hitbox=hitbox;
 }

 private void moved(){
  cupX=(upX+tx)*scale;
  cupY=(upY+ty)*scale;
  cupZ=(upZ+tz)*scale;
  clowX=(lowX+tx)*scale;
  clowY=(lowY+ty)*scale;
  clowZ=(lowZ+tz)*scale;
 }
 private FloatBuffer mFVertexBuffer;
 private FloatBuffer mFNormalBuffer;
 private ShortBuffer mIndexBuffer;
 private boolean colored;
 private FloatBuffer mFColorBuffer;
 private FloatBuffer boxVert;
 private ShortBuffer boxIndex;
 private int verts;
 private float tx;
 private float ty;
 private float tz;
 private float scale;
 private float upX;
 private float upY;
 private float upZ;
 private float lowX;
 private float lowY;
 private float lowZ;
 private float cupX;
 private float cupY;
 private float cupZ;
 private float clowX;
 private float clowY;
 private float clowZ;
 private boolean hitbox;
}
