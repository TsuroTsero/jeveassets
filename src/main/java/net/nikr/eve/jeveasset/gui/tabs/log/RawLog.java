/*
 * Copyright 2009-2017 Contributors (see credits.txt)
 *
 * This file is part of jEveAssets.
 *
 * jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package net.nikr.eve.jeveasset.gui.tabs.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.nikr.eve.jeveasset.data.api.my.MyAsset;
import net.nikr.eve.jeveasset.data.settings.LogManager.LogAsset;


public class RawLog implements Comparable<RawLog> {
	
	private final Date date;
	private final Long itemID;
	private final Integer typeID;
	private final Long count;
	private final Long ownerID;
	private final LogData oldData;
	private final LogData newData;
	private final Map<LogChangeType, List<LogType>> logTypes;

	/**
	 * Parent
	 * @param rawLog 
	 */
	protected RawLog(RawLog rawLog) {
		this.date = rawLog.date;
		this.itemID = rawLog.itemID;
		this.typeID = rawLog.typeID;
		this.count = rawLog.count;
		this.ownerID = rawLog.ownerID;
		this.oldData = rawLog.oldData;
		this.newData = rawLog.newData;
		this.logTypes = rawLog.logTypes;
	}

	/**
	 * Load
	 * @param asset
	 * @param date
	 * @param oldData
	 * @param newData 
	 * @param logTypes 
	 */
	public RawLog(LogAsset asset, Date date, LogData oldData, LogData newData, Map<LogChangeType, List<LogType>> logTypes) {
		this.date = date;
		this.itemID = asset.getItemID();
		this.typeID = asset.getTypeID();
		this.count = asset.getCount();
		this.ownerID = asset.getOwnerID();
		this.oldData = oldData;
		this.newData = newData;
		this.logTypes = logTypes;
	}

	/**
	 * Load
	 * @param date
	 * @param itemID
	 * @param typeID
	 * @param count
	 * @param ownerID
	 * @param oldData
	 * @param newData 
	 * @param logTypes 
	 */
	public RawLog(Date date, Long itemID, Integer typeID, Long count, Long ownerID, LogData oldData, LogData newData, Map<LogChangeType, List<LogType>> logTypes) {
		this.date = date;
		this.itemID = itemID;
		this.typeID = typeID;
		this.count = count;
		this.ownerID = ownerID;
		this.oldData = oldData;
		this.newData = newData;
		this.logTypes = logTypes;
	}

	public Date getDate() {
		return date;
	}

	public Long getItemID() {
		return itemID;
	}

	public Integer getTypeID() {
		return typeID;
	}

	public Long getCount() {
		return count;
	}

	public Long getOwnerID() {
		return ownerID;
	}

	public LogData getOldData() {
		return oldData;
	}

	public LogData getNewData() {
		return newData;
	}

	public Map<LogChangeType, List<LogType>> getLogTypes() {
		return logTypes;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + Objects.hashCode(this.date);
		hash = 31 * hash + Objects.hashCode(this.itemID);
		hash = 31 * hash + Objects.hashCode(this.typeID);
		hash = 31 * hash + Objects.hashCode(this.oldData);
		hash = 31 * hash + Objects.hashCode(this.newData);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RawLog other = (RawLog) obj;
		if (!Objects.equals(this.date, other.date)) {
			return false;
		}
		if (!Objects.equals(this.itemID, other.itemID)) {
			return false;
		}
		if (!Objects.equals(this.typeID, other.typeID)) {
			return false;
		}
		if (!Objects.equals(this.oldData, other.oldData)) {
			return false;
		}
		if (!Objects.equals(this.newData, other.newData)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(RawLog o) {
		return date.compareTo(o.date);
	}

	public static class LogData {
		private final long ownerID;
		private final long locationID;
		private final int flagID;
		private final String container;
		private final List<Long> parentIDs;

		public LogData(LogAsset asset) {
			this.ownerID = asset.getOwnerID();
			this.locationID = asset.getLocationID();
			this.flagID = asset.getFlagID();
			this.container = asset.getContainer();
			this.parentIDs = new ArrayList<Long>();
			for (MyAsset parent : asset.getParents()) {
				parentIDs.add(parent.getItemID());
			}
		}

		public LogData(long ownerID, long locationID, int flagID, String container, List<Long> parentIDs) {
			this.ownerID = ownerID;
			this.locationID = locationID;
			this.flagID = flagID;
			this.container = container;
			this.parentIDs = parentIDs;
		}

		public Long getOwnerID() {
			return ownerID;
		}

		public Long getLocationID() {
			return locationID;
		}

		public Integer getFlagID() {
			return flagID;
		}

		public String getContainer() {
			return container;
		}

		public List<Long> getParentIDs() {
			return parentIDs;
		}

		public static Map<LogChangeType, List<LogType>> changed(Date date, LogData oldData, LogData newData, int percent, int count, LogChangeType defaultChangeType) {
			if (oldData.ownerID != newData.ownerID) {
				return Collections.singletonMap(LogChangeType.MOVED_OWNER, Collections.singletonList(new LogType(date, LogChangeType.MOVED_OWNER, percent, count)));
			} else if (oldData.locationID != newData.locationID) {
				return Collections.singletonMap(LogChangeType.MOVED_LOCATION, Collections.singletonList(new LogType(date, LogChangeType.MOVED_LOCATION, percent, count)));
			} else if (oldData.flagID != newData.flagID) {
				return Collections.singletonMap(LogChangeType.MOVED_FLAG, Collections.singletonList(new LogType(date, LogChangeType.MOVED_FLAG, percent, count)));
			} else if (oldData.parentIDs.equals(newData.parentIDs)) {
				return Collections.singletonMap(LogChangeType.MOVED_CONTAINER, Collections.singletonList(new LogType(date, LogChangeType.MOVED_CONTAINER, percent, count)));
			} else {
				return Collections.singletonMap(defaultChangeType, Collections.singletonList(new LogType(date, defaultChangeType, percent, count)));
			}
		}

		public static LogType getLogType(Date date, LogData oldData, LogData newData, int percent, int count, LogChangeType defaultChangeType) {
			if (oldData.ownerID != newData.ownerID) {
				return new LogType(date, LogChangeType.MOVED_OWNER, percent, count);
			} else if (oldData.locationID != newData.locationID) {
				return new LogType(date, LogChangeType.MOVED_LOCATION, percent, count);
			} else if (oldData.flagID != newData.flagID) {
				return new LogType(date, LogChangeType.MOVED_FLAG, percent, count);
			} else if (oldData.parentIDs.equals(newData.parentIDs)) {
				return new LogType(date, LogChangeType.MOVED_CONTAINER, percent, count);
			} else {
				return new LogType(date, defaultChangeType, percent, count);
			}
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 19 * hash + (int) (this.ownerID ^ (this.ownerID >>> 32));
			hash = 19 * hash + (int) (this.locationID ^ (this.locationID >>> 32));
			hash = 19 * hash + this.flagID;
			hash = 19 * hash + Objects.hashCode(this.parentIDs);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final LogData other = (LogData) obj;
			if (this.ownerID != other.ownerID) {
				return false;
			}
			if (this.locationID != other.locationID) {
				return false;
			}
			if (this.flagID != other.flagID) {
				return false;
			}
			if (!Objects.equals(this.parentIDs, other.parentIDs)) {
				return false;
			}
			return true;
		}

		
	}
}
