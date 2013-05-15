/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.mobile.learning.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Module implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4412987572522420704L;
	
	public static final String TAG = Module.class.getSimpleName();
	private int modId;
	private String location;
	private ArrayList<Lang> titles = new ArrayList<Lang>();
	private String shortname;
	private float progress = 0;
	private HashMap<String,String> props;
	private Double versionId;
	private boolean installed;
	private boolean toUpdate;
	private boolean toUpdateSchedule;
	private String downloadUrl;
	private ArrayList<String> availableLangs = new ArrayList<String>();
	private String imageFile;
	private ArrayList<Media> media = new ArrayList<Media>();
	private ArrayList<ModuleMetaPage> metaPages = new ArrayList<ModuleMetaPage>();
	private Double scheduleVersionID;
	private String scheduleURI;
	
	public Module() {

	}	
	
	public Double getScheduleVersionID() {
		return scheduleVersionID;
	}

	public void setScheduleVersionID(Double scheduleVersionID) {
		this.scheduleVersionID = scheduleVersionID;
	}

	public ArrayList<Media> getMedia() {
		return media;
	}

	public void setMedia(ArrayList<Media> media) {
		this.media = media;
	}

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}
	
	public ArrayList<String> getAvailableLangs() {
		return availableLangs;
	}

	public void setAvailableLangs(ArrayList<String> availableLangs) {
		this.availableLangs = availableLangs;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	
	
	public Double getVersionId() {
		return versionId;
	}

	public void setVersionId(Double versionId) {
		this.versionId = versionId;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public boolean isToUpdate() {
		return toUpdate;
	}

	public void setToUpdate(boolean toUpdate) {
		this.toUpdate = toUpdate;
	}

	public HashMap<String, String> getProps() {
		return props;
	}

	public void setProps(HashMap<String, String> props) {
		this.props = props;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname.toLowerCase(Locale.US);
	}

	public int getModId() {
		return modId;
	}

	public void setModId(int modId) {
		this.modId = modId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTitle(String lang) {
		for(Lang l: titles){
			if(l.getLang().equals(lang)){
				return l.getContent();
			}
		}
		if(titles.size() > 0){
			return titles.get(0).getContent();
		}
		return "No title set";
	}
	
	public void setTitles(ArrayList<Lang> titles) {
		this.titles = titles;
	}
	
	public boolean hasMedia(){
		if(media.size() == 0){
			return false;
		} else {
			return true;
		}
	}
	
	public void setMetaPages(ArrayList<ModuleMetaPage> ammp){
		this.metaPages = ammp;
	}
	
	public ArrayList<ModuleMetaPage> getMetaPages(){
		return this.metaPages;
	}
	
	public ModuleMetaPage getMetaPage(int id){
		for(ModuleMetaPage mmp: this.metaPages){
			if(id == mmp.getId()){
				return mmp;
			}
		}
		return null;
	}

	public boolean isToUpdateSchedule() {
		return toUpdateSchedule;
	}

	public void setToUpdateSchedule(boolean toUpdateSchedule) {
		this.toUpdateSchedule = toUpdateSchedule;
	}

	public String getScheduleURI() {
		return scheduleURI;
	}

	public void setScheduleURI(String scheduleURI) {
		this.scheduleURI = scheduleURI;
	}
	
	
}
