package com.example.urbik;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.EPLAYBACK_STATUS;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.MovieTextureStatus;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;

public class Tracking3D extends ARViewActivity {

	private enum EState {
		INITIALIZATION, TRACKING
	};

	EState mState = EState.INITIALIZATION;

	/**
	 * Geometry
	 */
	private IGeometry mModel = null;
	private IGeometry mVizAidModel = null;
	private String[] drawerItemsList;
	private ListView myDrawer;
	/**
	 * metaio SDK callback handler
	 */
	private MetaioSDKCallbackHandler mCallbackHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCallbackHandler = new MetaioSDKCallbackHandler();
		drawerItemsList = getResources().getStringArray(R.array.items);
//		myDrawer = (ListView) findViewById(R.id.my_drawer);
//		myDrawer.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_item, drawerItemsList));
//	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	public void onButtonClick(View v) {
		finish();
	}

	public void onResetButtonClick(View v) {
		loadTrackingConfig();
	}

	@Override
	protected void loadContents() {
//		mModel = loadModel("Tracking3D/ConeHQ.obj");
//		mVizAidModel = loadModel("Tracking3D/Aide_Visuel.obj");
		
		mModel = loadModel("Tracking3D/V2/AugmentedContent/PLouvre/Pyramides_Louvre.obj");
		mVizAidModel = loadModel("Tracking3D/V2/AideVisuel/Pyramide_Socle.obj");
		mVizAidModel.setTransparency(0.8f);

		if (mModel != null){
			mModel.setCoordinateSystemID(1);		
		} 
		if (mVizAidModel != null){
			mVizAidModel.setCoordinateSystemID(2);
		}
		
		loadTrackingConfig();
		
	}

	void loadTrackingConfig() {
//		boolean result = setTrackingConfiguration("Tracking3D/TrackingData_EdgeBased_FreeIndoor.xml");
		boolean result = setTrackingConfiguration("Tracking3D/V2/ConfigurationContent/TrackingData_EdgeBased_FreeIndoor.xml");
		if (!result)
			MetaioDebug.log(Log.ERROR, "Failed to load tracking configuration.");
		

		mState = EState.INITIALIZATION;

	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {

		@Override
		public void onSDKReady() {
			// show GUI
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			if (trackingValues.size() > 0
					&& trackingValues.get(0).getState() == ETRACKING_STATE.ETS_REGISTERED) {
				mState = EState.TRACKING;
			}
		}

	}

	public IGeometry loadModel(final String path) {
		IGeometry geometry = null;
		try {
			// Load model
			String modelPath = AssetsManager.getAssetPath(
					getApplicationContext(), path);
			geometry = metaioSDK.createGeometry(modelPath);

			MetaioDebug.log("Loaded geometry " + modelPath);
		} catch (Exception e) {
			MetaioDebug.log(Log.ERROR,
					"Error loading geometry: " + e.getMessage());
			return geometry;
		}
		return geometry;
	}

	public boolean setTrackingConfiguration(final String path) {
		boolean result = false;
		try {
			// set tracking configuration
			String xmlPath = AssetsManager.getAssetPath(
					getApplicationContext(), path);
			result = metaioSDK.setTrackingConfiguration(xmlPath);
			MetaioDebug.log("Loaded tracking configuration " + xmlPath);
		} catch (Exception e) {
			MetaioDebug.log(Log.ERROR, "Error loading tracking configuration: "
					+ path + " " + e.getMessage());
			return result;
		}
		return result;
	}
	

	@Override
	protected int getGUILayout() {
		return R.layout.activity_tracking;
	}

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub
		
	}

	

}
