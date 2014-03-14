/*
This file is part of BeepMe.

BeepMe is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BeepMe is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BeepMe. If not, see <http://www.gnu.org/licenses/>.

Copyright 2012-2014 Michael Glanznig
http://beepme.glanznig.com
*/

package com.glanznig.beepme;

import java.util.Date;
import java.util.List;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.glanznig.beepme.helper.PhotoUtils;
import com.glanznig.beepme.view.SamplePhotoView;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SampleListAdapter extends ArrayAdapter<ListItem> {
	
	private final Context context;
	private final List<ListItem> samples;
	
	static class EntryHolder {
	    public TextView title;
	    public TextView description;
	    public TextView timestamp;
	    public SamplePhotoView photo;
	}
	
	static class HeaderHolder {
	    public TextView headerTitle;
	}
	
	private static class ImgLoadHandler extends Handler {
		WeakReference<SamplePhotoView> view;
		String uri;
		
		ImgLoadHandler(SamplePhotoView view, String uri) {
			this.view = new WeakReference<SamplePhotoView>(view);
			this.uri = uri;
		}
		
	    @Override
	    public void handleMessage(Message msg) {
	    	if (msg.what == PhotoUtils.MSG_PHOTO_LOADED) {
	    		Bitmap imageBitmap = (Bitmap)msg.obj;
	    		Bundle data = msg.getData();
	    		String uri = data.getString("uri");
	    		
	    		if (view.get() != null && imageBitmap != null && uri != null && this.uri != null) {
	    			if (this.uri.equals(uri)) {
	    				view.get().setPhoto(imageBitmap);
	    			}
	    			else {
	    				imageBitmap.recycle();
	    			}
	    		}
	    	}
	    }
	}
	
	public SampleListAdapter(Context context, List<ListItem> values) {
	    super(context, R.layout.list_sample_row, values);
	    this.context = context;
	    this.samples = values;
	}
	
	@Override
	public boolean isEnabled(int position) {
		ListItem item = samples.get(position);
		return !item.isSectionHeader();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT); 
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		ListItem item = samples.get(position);
		
		if (item.isSectionHeader()) {
			//performance optimization: reuse already inflated views
			if (convertView != null && convertView instanceof TextView) {
				rowView = convertView;
			}
			else {
				rowView = inflater.inflate(R.layout.list_sample_header, parent, false);
				
				HeaderHolder holder = new HeaderHolder();
				holder.headerTitle = (TextView)rowView.findViewById(R.id.sample_list_header_title);
				rowView.setTag(holder);
			}
			
			HeaderHolder holder = (HeaderHolder)rowView.getTag();
			
			if (((DateListSectionHeader)samples.get(position)).getDate() != null) {
				Date viewDate = ((DateListSectionHeader)samples.get(position)).getDate();
				String content = "";
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				if (format.format(new Date()).equals(format.format(viewDate))) {
					content = context.getString(R.string.today) + " "; 
				}
				content += dateFormat.format(viewDate);
				holder.headerTitle.setText(content);
			}
		}
		else {
			//performance optimization: reuse already inflated views
			if (convertView != null && convertView instanceof LinearLayout) {
				rowView = convertView;
			}
			else {
				rowView = inflater.inflate(R.layout.list_sample_row, parent, false);
				
				EntryHolder holder = new EntryHolder();
				holder.title = (TextView)rowView.findViewById(R.id.sample_title);
				holder.timestamp = (TextView)rowView.findViewById(R.id.sample_timestamp);
				holder.description = (TextView)rowView.findViewById(R.id.sample_description);
				holder.photo = (SamplePhotoView)rowView.findViewById(R.id.sample_photo);
				holder.photo.setRights(false, false); // read only
				rowView.setTag(holder);
			}
			
			EntryHolder holder = (EntryHolder)rowView.getTag();
			SampleListEntry entry = (SampleListEntry)samples.get(position);
			
			if (entry.getTitle() != null && entry.getTitle().length() > 0) {
				String entryTitle = entry.getTitle();
				holder.title.setText(entryTitle);
			}
			else {
				holder.title.setText(R.string.sample_untitled);
			}
			
			if (entry.getPhoto() != null && entry.getPhoto().length() > 0) {
				String thumbnailUri = PhotoUtils.getThumbnailUri(entry.getPhoto(), 64);
				File thumb = new File(thumbnailUri);
				if (thumb.exists()) {
					ImgLoadHandler handler = new ImgLoadHandler(holder.photo, thumbnailUri);
					PhotoUtils.getAsyncBitmap(this.getContext(), thumbnailUri, handler);
				}
				else {
					holder.photo.unsetPhoto();
					holder.photo.measure(0, 0);
					PhotoUtils.generateThumbnail(entry.getPhoto(), 64, holder.photo.getMeasuredWidth(), null);
				}
			}
			else {
				holder.photo.unsetPhoto();
			}
			
			if (entry.getDescription() != null && entry.getDescription().length() > 0) {
				String entryDescr = entry.getDescription();
				holder.description.setText(entryDescr);
				holder.description.setVisibility(View.VISIBLE);
			}
			else {
				holder.description.setVisibility(View.GONE);
			}
				
			holder.timestamp.setText(timeFormat.format(entry.getTimestamp()));
		}
		
		return rowView;
	}

}
