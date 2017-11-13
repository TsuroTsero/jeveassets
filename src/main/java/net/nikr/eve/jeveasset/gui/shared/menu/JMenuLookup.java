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

package net.nikr.eve.jeveasset.gui.shared.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.nikr.eve.jeveasset.Program;
import net.nikr.eve.jeveasset.gui.images.Images;
import net.nikr.eve.jeveasset.gui.shared.menu.MenuManager.JAutoMenu;
import net.nikr.eve.jeveasset.gui.tabs.overview.OverviewTab;
import net.nikr.eve.jeveasset.gui.tabs.overview.OverviewTab.OverviewAction;
import net.nikr.eve.jeveasset.i18n.GuiShared;
import net.nikr.eve.jeveasset.i18n.TabsOverview;
import net.nikr.eve.jeveasset.io.shared.DesktopUtil;


public class JMenuLookup<T> extends JAutoMenu<T> {

	private enum MenuLookupAction {
		EVE_CENTRAL,
		EVE_MARKETDATA,
		EVE_MARKETS,
		EVEMARKETER,
		GAMES_CHRUKER,
		//EVE_ITEM_DATABASE,
		EVEMAPS_DOTLAN_STATION,
		EVEMAPS_DOTLAN_SYSTEM,
		EVEMAPS_DOTLAN_REGION,
		//EVEMARKETEER,
		//EVE_ADDICTS
	}

	private final JMenu jDotlan;
	private final JMenuItem jDotlanStation;
	private final JMenuItem jDotlanSystem;
	private final JMenuItem jDotlanRegion;
	private final JMenuItem jDotlanLocations;
	private final JMenuItem jEveCentral;
	private final JMenuItem jEveMarketdata;
	private final JMenuItem jEveMarketer;
	//Dead
	//private final JMenuItem jEveMarketeer;
	private final JMenuItem jEveMarkets;
	//Dead
	//private final JMenuItem jEveAddicts;
	private final JMenuItem jChruker;
	//Dead
	//private final JMenuItem jEveOnline;

	private MenuData<T> menuData;

	public JMenuLookup(final Program program) {
		super(GuiShared.get().lookup(), program);

		ListenerClass listener = new ListenerClass();

		this.setIcon(Images.LINK_LOOKUP.getIcon());

		jDotlan = new JMenu(GuiShared.get().dotlan());
		jDotlan.setIcon(Images.LINK_DOTLAN_EVEMAPS.getIcon());
		add(jDotlan);

		jDotlanStation = new JMenuItem(GuiShared.get().station());
		jDotlanStation.setIcon(Images.LOC_STATION.getIcon());
		jDotlanStation.setActionCommand(MenuLookupAction.EVEMAPS_DOTLAN_STATION.name());
		jDotlanStation.addActionListener(listener);
		jDotlan.add(jDotlanStation);

		jDotlanSystem = new JMenuItem(GuiShared.get().system());
		jDotlanSystem.setIcon(Images.LOC_SYSTEM.getIcon());
		jDotlanSystem.setActionCommand(MenuLookupAction.EVEMAPS_DOTLAN_SYSTEM.name());
		jDotlanSystem.addActionListener(listener);
		jDotlan.add(jDotlanSystem);

		jDotlanRegion = new JMenuItem(GuiShared.get().region());
		jDotlanRegion.setIcon(Images.LOC_REGION.getIcon());
		jDotlanRegion.setActionCommand(MenuLookupAction.EVEMAPS_DOTLAN_REGION.name());
		jDotlanRegion.addActionListener(listener);
		jDotlan.add(jDotlanRegion);

		jDotlanLocations = new JMenuItem(TabsOverview.get().locations());
		jDotlanLocations.setIcon(Images.LOC_LOCATIONS.getIcon());

		addSeparator();

		jEveCentral = new JMenuItem(GuiShared.get().eveCentral());
		jEveCentral.setIcon(Images.LINK_EVE_CENTRAL.getIcon());
		jEveCentral.setActionCommand(MenuLookupAction.EVE_CENTRAL.name());
		jEveCentral.addActionListener(listener);
		add(jEveCentral);

		jEveMarketdata = new JMenuItem(GuiShared.get().eveMarketdata());
		jEveMarketdata.setIcon(Images.LINK_EVE_MARKETDATA.getIcon());
		jEveMarketdata.setActionCommand(MenuLookupAction.EVE_MARKETDATA.name());
		jEveMarketdata.addActionListener(listener);
		add(jEveMarketdata);

		//Dead
		/*
		jEveMarketeer = new JMenuItem(GuiShared.get().eveMarketeer());
		jEveMarketeer.setIcon(Images.LINK_EVEMARKETEER.getIcon());
		jEveMarketeer.setActionCommand(MenuLookupAction.EVEMARKETEER.name());
		jEveMarketeer.addActionListener(listener);
		add(jEveMarketeer);
		*/

		jEveMarkets = new JMenuItem(GuiShared.get().eveMarkets());
		jEveMarkets.setIcon(Images.LINK_EVE_MARKETS.getIcon());
		jEveMarkets.setActionCommand(MenuLookupAction.EVE_MARKETS.name());
		jEveMarkets.addActionListener(listener);
		add(jEveMarkets);

		
		jEveMarketer = new JMenuItem(GuiShared.get().eveMarketer());
		jEveMarketer.setIcon(Images.LINK_EVEMARKETER.getIcon());
		jEveMarketer.setActionCommand(MenuLookupAction.EVEMARKETER.name());
		jEveMarketer.addActionListener(listener);
		add(jEveMarketer);

		//Dead
		/*
		jEveAddicts = new JMenuItem(GuiShared.get().eveAddicts());
		jEveAddicts.setIcon(Images.LINK_EVE_ADDICTS.getIcon());
		jEveAddicts.setActionCommand(MenuLookupAction.EVE_ADDICTS.name());
		jEveAddicts.addActionListener(listener);
		add(jEveAddicts);
		*/

		addSeparator();

		jChruker = new JMenuItem(GuiShared.get().chruker());
		jChruker.setIcon(Images.LINK_CHRUKER.getIcon());
		jChruker.setActionCommand(MenuLookupAction.GAMES_CHRUKER.name());
		jChruker.addActionListener(listener);
		add(jChruker);

		//Dead
		/*
		jEveOnline = new JMenuItem(GuiShared.get().eveOnline());
		jEveOnline.setIcon(Images.MISC_EVE.getIcon());
		jEveOnline.setActionCommand(MenuLookupAction.EVE_ITEM_DATABASE.name());
		jEveOnline.addActionListener(listener);
		add(jEveOnline);
		*/
	}

	@Override
	public void setMenuData(MenuData<T> menuData) {
		this.menuData = menuData;
		jDotlan.setEnabled(!menuData.getStationNames().isEmpty() || !menuData.getSystemNames().isEmpty() || !menuData.getRegionNames().isEmpty());
		jDotlanStation.setEnabled(!menuData.getStationNames().isEmpty());
		jDotlanSystem.setEnabled(!menuData.getSystemNames().isEmpty());
		jDotlanRegion.setEnabled(!menuData.getRegionNames().isEmpty());
		jEveCentral.setEnabled(!menuData.getMarketTypeIDs().isEmpty());
		jEveMarketdata.setEnabled(!menuData.getMarketTypeIDs().isEmpty());
		jEveMarketer.setEnabled(!menuData.getMarketTypeIDs().isEmpty());
		//Dead
		//jEveMarketeer.setEnabled(!menuData.getMarketTypeIDs().isEmpty());
		jEveMarkets.setEnabled(!menuData.getMarketTypeIDs().isEmpty());
		//Dead
		//jEveAddicts.setEnabled(!menuData.getMarketTypeIDs().isEmpty());
		jChruker.setEnabled(!menuData.getTypeIDs().isEmpty());
		//Dead
		//jEveOnline.setEnabled(!menuData.getTypeNames().isEmpty());
	}

	public void setTool(Object object) {
		if (object instanceof OverviewTab) {
			OverviewTab overviewTab = (OverviewTab) object;
			//Remove all action listeners
			for (ActionListener listener : jDotlanLocations.getActionListeners()) {
				jDotlanLocations.removeActionListener(listener);
			}
			boolean enabled = overviewTab.isGroupAndNotEmpty();
			jDotlanLocations.setActionCommand(OverviewAction.GROUP_LOOKUP.name());
			jDotlanLocations.addActionListener(overviewTab.getListenerClass());
			jDotlanLocations.setEnabled(enabled);
			jDotlan.add(jDotlanLocations);
			jDotlan.setEnabled(enabled || !overviewTab.isGroup());
		} else {
			jDotlan.remove(jDotlanLocations);
		}
	}

	public static void browseDotlan(final Program program, Set<String> stations, Set<String> systems, Set<String> regions) {
		Set<String> urls = new HashSet<String>();
		if (stations != null) {
			for (String station : stations) {
				urls.add("http://evemaps.dotlan.net/outpost/" + station.replace(" ", "_"));
			}
		}
		if (systems != null) {
			for (String system : systems) {
				urls.add("http://evemaps.dotlan.net/system/" + system.replace(" ", "_"));
			}
		}
		if (regions != null) {
			for (String region : regions) {
				urls.add("http://evemaps.dotlan.net/map/" + region.replace(" ", "_"));
			}
		}
		DesktopUtil.browse(urls, program);
	}

	private class ListenerClass implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			if (MenuLookupAction.EVEMAPS_DOTLAN_STATION.name().equals(e.getActionCommand())) {
				browseDotlan(program, menuData.getStationNames(), null, null);
			}
			if (MenuLookupAction.EVEMAPS_DOTLAN_SYSTEM.name().equals(e.getActionCommand())) {
				browseDotlan(program, null, menuData.getSystemNames(), null);
			}
			if (MenuLookupAction.EVEMAPS_DOTLAN_REGION.name().equals(e.getActionCommand())) {
				browseDotlan(program, null, null, menuData.getRegionNames());
			}
			if (MenuLookupAction.EVE_CENTRAL.name().equals(e.getActionCommand())) {
				Set<String> urls = new HashSet<String>();
				for (int marketTypeID : menuData.getMarketTypeIDs()) {
					urls.add("http://www.eve-central.com/home/quicklook.html?typeid=" + marketTypeID);
				}
				DesktopUtil.browse(urls, program);
			}
			if (MenuLookupAction.EVE_MARKETDATA.name().equals(e.getActionCommand())) {
				Set<String> urls = new HashSet<String>();
				for (int marketTypeID : menuData.getMarketTypeIDs()) {
					urls.add("http://eve-marketdata.com/price_check.php?type_id=" + marketTypeID);
				}
				DesktopUtil.browse(urls, program);
			}
			if (MenuLookupAction.EVEMARKETER.name().equals(e.getActionCommand())) {
				Set<String> urls = new HashSet<String>();
				for (int marketTypeID : menuData.getMarketTypeIDs()) {
					urls.add("https://evemarketer.com/types/" + marketTypeID);
				}
				DesktopUtil.browse(urls, program);
			}
			//Dead
			/*
			if (MenuLookupAction.EVEMARKETEER.name().equals(e.getActionCommand())) {
				if (!confirmOpenLinks(program, menuData.getMarketTypeIDs().size())) {
					return;
				}
				for (int marketTypeID : menuData.getMarketTypeIDs()) {
					DesktopUtil.browse("http://www.evemarketeer.com/item/info/" + marketTypeID, program);
				}
			}
			*/
			if (MenuLookupAction.EVE_MARKETS.name().equals(e.getActionCommand())) {
				Set<String> urls = new HashSet<String>();
				for (int marketTypeID : menuData.getMarketTypeIDs()) {
					urls.add("http://www.eve-markets.net/detail.php?typeid=" + marketTypeID);
				}
				DesktopUtil.browse(urls, program);
			}
			//Dead
			/*
			if (MenuLookupAction.EVE_ADDICTS.name().equals(e.getActionCommand())) {
				if (!confirmOpenLinks(program, menuData.getMarketTypeIDs().size())) {
					return;
				}
				for (int marketTypeID : menuData.getMarketTypeIDs()) {
					DesktopUtil.browse("http://eve.addicts.nl/?typeID=" + marketTypeID, program);
				}
			}
			*/
			if (MenuLookupAction.GAMES_CHRUKER.name().equals(e.getActionCommand())) {
				Set<String> urls = new HashSet<String>();
				for (int typeID : menuData.getTypeIDs()) {
					urls.add("http://games.chruker.dk/eve_online/item.php?type_id=" + typeID);
				}
				DesktopUtil.browse(urls, program);
			}
			//Dead
			/*
			if (MenuLookupAction.EVE_ITEM_DATABASE.name().equals(e.getActionCommand())) {
				if (!confirmOpenLinks(program, menuData.getTypeNames().size())) {
					return;
				}
				for (String typeName : menuData.getTypeNames()) {
					DesktopUtil.browse("http://wiki.eveonline.com/wiki/" + typeName.replace(" ", "_"), program);
				}
			}
			*/
		}
	}
}
