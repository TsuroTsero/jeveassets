/*
 * Copyright 2009-2024 Contributors (see credits.txt)
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
package net.nikr.eve.jeveasset.gui.dialogs.settings;

import java.awt.Color;
import java.util.Comparator;
import net.nikr.eve.jeveasset.data.settings.ColorSettings.ColorRow;
import net.nikr.eve.jeveasset.gui.shared.table.EnumTableColumn;
import net.nikr.eve.jeveasset.i18n.DialoguesSettings;

public enum ColorsTableFormat implements EnumTableColumn<ColorRow> {
	NAME(String.class) {
		@Override
		public String getColumnName() {
			return DialoguesSettings.get().columnName();
		}
		@Override
		public Object getColumnValue(final ColorRow from) {
			return from.getColorEntry().getDescription();
		}
	},
	BACKGROUND(Color.class) {
		@Override
		public String getColumnName() {
			return DialoguesSettings.get().columnBackground();
		}
		@Override
		public Object getColumnValue(final ColorRow from) {
			return from.getBackground();
		}
	},
	FOREGROUND(Color.class) {
		@Override
		public String getColumnName() {
			return DialoguesSettings.get().columnForeground();
		}
		@Override
		public Object getColumnValue(final ColorRow from) {
			return from.getForeground();
		}
	},
	PREVIEW(String.class) {
		@Override
		public String getColumnName() {
			return DialoguesSettings.get().columnPreview();
		}
		@Override
		public Object getColumnValue(final ColorRow from) {
			return DialoguesSettings.get().testText();
		}
	},
	SELECTED(String.class) {
		@Override
		public String getColumnName() {
			return DialoguesSettings.get().columnSelected();
		}
		@Override
		public Object getColumnValue(final ColorRow from) {
			return DialoguesSettings.get().testSelectedText();
		}
	};

	private final Class<?> type;
	private final Comparator<?> comparator;

	private ColorsTableFormat(final Class<?> type) {
		this.type = type;
		this.comparator = EnumTableColumn.getComparator(type);
	}
	@Override
	public Class<?> getType() {
		return type;
	}
	@Override
	public Comparator<?> getComparator() {
		return comparator;
	}
	@Override
	public String toString() {
		return getColumnName();
	}

}
