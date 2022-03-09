package apertx.teddy;
import android.content.*;
import android.opengl.*;

import static android.opengl.GLES10.*;
import android.graphics.*;
import java.io.*;
import java.nio.*;
import javax.microedition.khronos.opengles.*;

public class UI{
 public UI(Context c,int pad){
  BitmapFactory.Options opt=new BitmapFactory.Options();
  opt.inScaled=false;
  InputStream is=c.getResources().openRawResource(pad);
  Bitmap bm=BitmapFactory.decodeStream(is,null,opt);
  int[]textures=new int[1];
  glGenTextures(1,textures,0);
  padTexture=textures[0];
  glBindTexture(GL_TEXTURE_2D,padTexture);
  GLUtils.texImage2D(GL_TEXTURE_2D,0,bm,0);
  bm.recycle();

  ByteBuffer vbb=ByteBuffer.allocateDirect(6*8);
  vbb.order(ByteOrder.nativeOrder());
  padVert=vbb.asFloatBuffer();
  ByteBuffer tbb=ByteBuffer.allocateDirect(6*8);
  tbb.order(ByteOrder.nativeOrder());
  padTex=tbb.asFloatBuffer();
  padTex.put(0).put(0).
   put(1).put(0).
   put(1).put(1).
   put(1).put(1).
   put(0).put(1).
   put(0).put(0);
  padTex.position(0);
 }

 public void draw(GL10 gl){
  glMatrixMode(GL_PROJECTION);
  glPushMatrix();
  glLoadIdentity();
  glOrthof(-rw,rw,-rh,rh,1.0f,2.0f);
  glMatrixMode(GL_MODELVIEW);
  glPushMatrix();
  glLoadIdentity();
  GLU.gluLookAt(gl,0,0,-1,0,0,0,0,1,0);
  glDisable(GL_LIGHTING);
  glDisableClientState(GL_NORMAL_ARRAY);
  glEnableClientState(GL_TEXTURE_COORD_ARRAY);
  glVertexPointer(2,GL_FLOAT,0,padVert);
  glTexCoordPointer(2,GL_FLOAT,0,padTex);
  glBindTexture(GL_TEXTURE_2D,padTexture);
  glDrawArrays(GL_TRIANGLES,0,6);
  glDisableClientState(GL_TEXTURE_COORD_ARRAY);
  glEnableClientState(GL_NORMAL_ARRAY);
  glEnable(GL_LIGHTING);
  glPopMatrix();
  glMatrixMode(GL_PROJECTION);
  glPopMatrix();
  glMatrixMode(GL_MODELVIEW);
 }
 public void resize(int width,int height){
  this.width=width;
  this.height=height;
  rw=1.0f;
  rh=1.0f;
  if(width>height)rw=(float)width/height;
  else rh=(float)height/width;
  padL=rw-0.125f;
  padB=-rh+0.125f;
  padR=rw-0.75f;
  padT=-rh+0.75f;
  padVert.clear();
  padVert.put(padL).put(padT).
   put(padR).put(padT).
   put(padR).put(padB).
   put(padR).put(padB).
   put(padL).put(padB).
   put(padL).put(padT);
  padVert.position(0);
 }
 public boolean inDpad(float x,float y){
  return x<padR&&x>padL&&y<padT&&y>padB;
 }

 private int width;
 private int height;
 private float rw;
 private float rh;
 private FloatBuffer padVert;
 private FloatBuffer padTex;
 private int padTexture;
 private float padT;
 private float padL;
 private float padB;
 private float padR;
}
