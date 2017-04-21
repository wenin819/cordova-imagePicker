/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";
	 
	private CallbackContext callbackContext;
	private JSONObject params;
	 
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
			Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
			int max = 20;
			int desiredWidth = 0;
			int desiredHeight = 0;
			int quality = 100;
			if (this.params.has("maximumImagesCount")) {
				max = this.params.getInt("maximumImagesCount");
			}
			if (this.params.has("width")) {
				desiredWidth = this.params.getInt("width");
			}
			if (this.params.has("height")) {
				desiredHeight = this.params.getInt("height");
			}
			if (this.params.has("quality")) {
				quality = this.params.getInt("quality");
			}
			if (this.params.has("maxShowCount")) {
				intent.putExtra("MAX_SHOW_COUNT", this.params.getInt("maxShowCount"));
			}
			intent.putExtra("MAX_IMAGES", max);
			intent.putExtra("WIDTH", desiredWidth);
			intent.putExtra("HEIGHT", desiredHeight);
			intent.putExtra("QUALITY", quality);
			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
			}
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			ArrayList<String> realFileNames = data.getStringArrayListExtra("MULTIPLEREALFILENAMES");

			JSONArray resultList = new JSONArray();
			for(int i = 0; i < fileNames.size(); i++ ){
				String path = formatPath(fileNames.get(i));
				String realPath = formatPath(realFileNames.get(i));
				File file = new File(realPath);
				JSONObject map = new JSONObject();
				long date = file.lastModified();
				try {
					map.put("date", date);
					map.put("img", ImageUtil.imgToBase64(path));
					map.put("filePath", path);
					map.put("realPath", realPath);
					resultList.put(map);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			this.callbackContext.success(resultList);
		} else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			JSONArray res = new JSONArray();
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
	}

	private String formatPath(String path) {
		return null != path && path.startsWith("file://") ? path.replace("file://", "") : path;
	}
}
