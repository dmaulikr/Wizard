package com.wizardfight;

import java.util.ArrayList;
import java.util.EnumMap;

import com.wizardfight.components.Vector3d;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


class SensorAndSoundThread extends Thread implements SensorEventListener {
	private static final boolean D = false;
	protected static boolean ORIENTATION_HORIZONTAL;
	private boolean listening;
	private boolean soundPlaying;
	private Looper mLooper;
	private final SensorManager mSensorManager;
	private final Sensor mAccelerometer;
	private ArrayList<Vector3d> records;
	private SoundPool soundPool;
	private final int wandSoundID;
	private int wandStreamID;
	private final EnumMap<Shape, Integer> shapeSoundIDs;
	private final EnumMap<Buff, Integer> buffSoundIDs;

	public SensorAndSoundThread(Context context, SensorManager sm, Sensor s) {
		setName("Sensor and Sound thread");
		mSensorManager = sm;
		mAccelerometer = s;
		soundPlaying = true;
		listening = false;
		// Initialize sound
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		wandSoundID = soundPool.load(context, R.raw.magic, 1);
		wandStreamID = -1;
		
		shapeSoundIDs = new EnumMap<Shape, Integer>(Shape.class);
		shapeSoundIDs.put(Shape.TRIANGLE,
				soundPool.load(context, R.raw.triangle_sound, 1));
		shapeSoundIDs.put(Shape.CIRCLE,
				soundPool.load(context, R.raw.circle_sound, 1));
		shapeSoundIDs.put(Shape.SHIELD,
				soundPool.load(context, R.raw.shield_sound, 1));
		shapeSoundIDs.put(Shape.Z,
				soundPool.load(context, R.raw.z_sound, 1));
		shapeSoundIDs.put(Shape.V,
				soundPool.load(context, R.raw.v_sound, 1));
		shapeSoundIDs.put(Shape.PI,
				soundPool.load(context, R.raw.pi_sound, 1));
		shapeSoundIDs.put(Shape.CLOCK,
				soundPool.load(context, R.raw.clock_sound, 1));
		
		buffSoundIDs = new EnumMap<Buff, Integer>(Buff.class);
		buffSoundIDs.put(Buff.HOLY_SHIELD, 
				soundPool.load(context, R.raw.buff_off_shield_sound, 1));
	}

	public void playShapeSound(Shape shape) {
		if (D) Log.e("Wizard Fight", "[shape] sound playing?: " + soundPlaying);
		
		Integer soundID = shapeSoundIDs.get(shape);
		if( soundPlaying && soundID != null ) {
		    soundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}

	public void playBuffSound(Buff buff) {
		Integer soundID = buffSoundIDs.get(buff);
		if( soundPlaying && soundID != null ) {
			soundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}
	
	public void run() {

//        Log.e("accThread",Thread.currentThread().getName());
		Looper.prepare();
		Handler handler = new Handler();
		mLooper = Looper.myLooper();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME, handler);
		Looper.loop();
	}

	public void startGettingData() {
		if (D) Log.e("Wizard Fight", "start getting data called");
		records = new ArrayList<Vector3d>();
		listening = true;
		if(!soundPlaying) return;
		if (wandStreamID == -1) {
			wandStreamID = soundPool.play(wandSoundID, 0.25f, 0.25f, 0, -1, 1);
			if (D) Log.e("Wizard Fight", "wand stream id: " + wandStreamID);
		} else {
			soundPool.resume(wandStreamID);
		}
	}

	public void stopGettingData() {
		listening = false;
		soundPool.pause(wandStreamID);
	}

	public ArrayList<Vector3d> stopAndGetResult() {
		listening = false;
		soundPool.pause(wandStreamID);
		return resize(records,50);
	}

	public void stopLoop() {
		mSensorManager.unregisterListener(this);
		if (mLooper != null)
			mLooper.quit();
		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
			if (D) Log.e("Wizard Fight", "sound pool stop and release");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
//        Log.e("accThread",Thread.currentThread().getName());
		if (!listening)
			return;
		if (records.size() > 1000) return;
		double x, y, z;
		if(ORIENTATION_HORIZONTAL) {
			x = event.values[1];
			y = -event.values[0];
			z = event.values[2];
		} else {
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
		}
		double len = Math.sqrt(x * x + y * y + z * z);
		Vector3d rec = new Vector3d(x , y, z);
		records.add(rec);
		if (D) Log.e("Wizard Fight", "size: " + records.size());
		float amplitude = (float) len / 10 + 0.1f;
		if (amplitude > 1.0f)
			amplitude = 1.0f;
		soundPool.setVolume(wandStreamID, amplitude, amplitude);
	}
    private static ArrayList<Vector3d> resize(ArrayList<Vector3d> a,int size)
    {
        if(a.size()<size)return a;
        ArrayList<Vector3d> s=new ArrayList<Vector3d>();
        double step=a.size()/(double)(size);
        for (int i = 0; i < size; i++) {
            s.add(getArrayResizeItem(a, step * i));
        }
        return s;
    }
    private static Vector3d getArrayResizeItem(ArrayList<Vector3d> a, double i){
        if(((i==((int)i))))
            return a.get((int)i);
        if(i+1>=a.size())
            return a.get(a.size()-1);
        double fPart = i % 1;
        double x= a.get((int)i).x+( a.get((int)i+1).x- a.get((int)i).x)*fPart;
        double y= a.get((int)i).y+( a.get((int)i+1).y- a.get((int)i).y)*fPart;
        double z= a.get((int)i).z+( a.get((int)i+1).z- a.get((int)i).z)*fPart;
        return new Vector3d(x,y,z);
    }
}
