package com.example.urbik;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class WifiActivity extends Activity  {
	Button searchNetwork;
	WifiManager wifiManager;
	WifiConfiguration conf = new WifiConfiguration();
	final static String networkSSID = "Urbik";
	static int level = 0;
	String currentSSID;
	TextView tNetwork;
	VideoView vid;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);
		searchNetwork = (Button) findViewById(R.id.b_searchNetwork);
		tNetwork = (TextView) findViewById(R.id.tv_Network);
		configurerReseau();
	}

	public void configurerReseau() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		conf.SSID = "\"" + networkSSID + "\"";
		conf.status = WifiConfiguration.Status.ENABLED;
		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		wifiManager.addNetwork(conf);
		currentSSID = (String) wifiManager.getConnectionInfo().getSSID();
		Log.i("CurrentSSID", currentSSID);
		System.out.println(currentSSID + " - " + networkSSID);
		if (currentSSID.equals(conf.SSID) && wifiManager.isWifiEnabled())
			tNetwork.setText("Vous êtes connecté au réseau " + conf.SSID);
		else {
			wifiManager.setWifiEnabled(true);
			wifiManager.startScan();
			IntentFilter intent = new IntentFilter();
			intent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			BroadcastReceiver receiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					List<ScanResult> scanRes = wifiManager.getScanResults();
					for (int i = 0; i < scanRes.size(); i++) {
						ScanResult retS = scanRes.get(i);
						if (retS.SSID.equals(networkSSID)) {
							// Ajout d'Urbik aux réseaux wifi du terminal
							int networkId = -1;
							if (wifiManager.getConfiguredNetworks() != null) {
								for (WifiConfiguration configuredNetwork : wifiManager
										.getConfiguredNetworks()) {
									if (conf.SSID
											.equals(configuredNetwork.SSID))
										networkId = configuredNetwork.networkId;
								}
							}
							if (networkId == -1)
								networkId = wifiManager.addNetwork(conf);
							else
								wifiManager.updateNetwork(conf);

							wifiManager.enableNetwork(networkId, true);
							try {
								Thread.sleep(2000L);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}

					}

				}

			};
			registerReceiver(receiver, intent);
		}
	}

	public void telecharger(View v) {
		vid = (VideoView) findViewById(R.id.v_PresentationUrbik);
		new BackgroundVideoTask().execute("http://192.168.1.111/www/urbik.mp4");
	}

	public int getWifiStrength() {
		try {
			int rssi = wifiManager.getConnectionInfo().getRssi();
			level = WifiManager.calculateSignalLevel(rssi, 100);
			return level;
		} catch (Exception e) {
			return 0;
		}
	}

	private class BackgroundVideoTask extends AsyncTask<String, Uri, Void> {
		ProgressDialog dialog;

		// Dialog signalant le chargement de la vidéo
		protected void onPreExecute() {
			dialog = new ProgressDialog(WifiActivity.this);
			dialog.setMessage("Chargement de votre vidéo ...");
			dialog.setCancelable(true);
			dialog.show();
		}

		// Récupère la vidéo à partir de l'url fournit dans l'appel de la
		// fonction
		protected void onProgressUpdate(final Uri... uri) {

			try {

				MediaController media = new MediaController(WifiActivity.this);
				vid.setMediaController(media);
				vid.setVideoURI(uri[0]);
				vid.requestFocus();
				vid.setOnPreparedListener(new OnPreparedListener() {

					public void onPrepared(MediaPlayer arg0) {
						vid.start();
						dialog.dismiss();
					}
				});

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				Uri uri = Uri.parse(params[0]);
				publishProgress(uri);
			} catch (Exception e) {
				e.printStackTrace();

			}
			return null;
		}

	}

}
