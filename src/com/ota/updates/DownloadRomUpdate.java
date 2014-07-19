/*
 * Copyright (C) 2014 Matt Booth (Kryten2k35).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ota.updates;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.ota.updates.tasks.UpdateProgress;
import com.ota.updates.utils.Constants;
import com.ota.updates.utils.Preferences;
import com.ota.updates.utils.Utils;

public class DownloadRomUpdate implements Constants{
	
	public final String TAG = this.getClass().getSimpleName();

	public DownloadRomUpdate(){
		
	}
	
	public static void startDownload(Context context){
		String url = RomUpdate.getDirectUrl(context);
		String fileName = RomUpdate.getFilename(context) + ".zip";
		String description = context.getResources().getString(R.string.downloading);
		File file = RomUpdate.getFullFile(context);
		
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

		if(Preferences.getNetworkType(context).equals("2")){
			// All network types are enabled by default
			// So if we choose Wi-Fi only, then enable the restriction
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		} 
		
		request.setTitle(fileName);
		request.setDescription(description);

		request.setVisibleInDownloadsUi(true);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
		
		// Delete any existing files
		Utils.deleteFile(file);

		// Enqueue the download
		DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		long mDownloadID = downloadManager.enqueue(request);

		// Store the download ID
		Preferences.setDownloadID(context, mDownloadID);
		
		// Set a setting indicating the download is now running
		Preferences.setIsDownloadRunning(context, true);
		
		// Start updating the progress
		new UpdateProgress(context, downloadManager).execute(mDownloadID);
		
		// MD5 checker has not been run, nor passed
		Preferences.setMD5Passed(context, false);
		Preferences.setHasMD5Run(context, false);
	}
	
	public static void cancelDownload(Context context){
		// Grab the download ID from settings
		long mDownloadID = Preferences.getDownloadID(context);
		
		// Remove the download
		DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		downloadManager.remove(mDownloadID);
		
		// Indicate that the download is no longer running
		Preferences.setIsDownloadRunning(context, false);		
	}
}