/*
 * Copyright 2009-2022 Contributors (see credits.txt)
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
package net.nikr.eve.jeveasset.gui.tabs.stockpile;

import ca.odell.glazedlists.EventList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.nikr.eve.jeveasset.Program;
import net.nikr.eve.jeveasset.data.api.accounts.OwnerType;
import net.nikr.eve.jeveasset.data.api.my.MyAsset;
import net.nikr.eve.jeveasset.data.api.my.MyContractItem;
import net.nikr.eve.jeveasset.data.api.my.MyIndustryJob;
import net.nikr.eve.jeveasset.data.api.my.MyMarketOrder;
import net.nikr.eve.jeveasset.data.api.my.MyTransaction;
import net.nikr.eve.jeveasset.data.profile.ProfileData;
import net.nikr.eve.jeveasset.data.profile.ProfileManager;
import net.nikr.eve.jeveasset.data.profile.TableData;
import net.nikr.eve.jeveasset.data.sde.ItemFlag;
import net.nikr.eve.jeveasset.data.sde.StaticData;
import net.nikr.eve.jeveasset.data.settings.Settings;
import net.nikr.eve.jeveasset.gui.shared.table.EventListManager;
import net.nikr.eve.jeveasset.gui.tabs.stockpile.Stockpile.StockpileFilter;
import net.nikr.eve.jeveasset.gui.tabs.stockpile.Stockpile.StockpileItem;
import net.nikr.eve.jeveasset.gui.tabs.stockpile.Stockpile.StockpileTotal;
import net.nikr.eve.jeveasset.gui.tabs.stockpile.Stockpile.SubpileItem;
import net.nikr.eve.jeveasset.gui.tabs.stockpile.Stockpile.SubpileStock;
import net.nikr.eve.jeveasset.io.shared.ApiIdConverter;


public class StockpileData extends TableData {

	private Map<Long, String> ownersName;
	private final Map<Integer, Set<MyContractItem>> contractItems = new HashMap<>();
	private final Map<Integer, Set<MyAsset>> assets = new HashMap<>();
	private final Map<Integer, Set<MyMarketOrder>> marketOrders = new HashMap<>();
	private final Map<Integer, Set<MyIndustryJob>> industryJobs = new HashMap<>();
	private final Map<Integer, Set<MyTransaction>> transactions = new HashMap<>();

	public StockpileData(Program program) {
		super(program);
	}

	public StockpileData(ProfileManager profileManager, ProfileData profileData) {
		super(profileManager, profileData);
	}

	public EventList<StockpileItem> getData() {
		EventList<StockpileItem> eventList = EventListManager.create();
		updateData(eventList);
		return eventList;
	}

	public void updateData(EventList<StockpileItem> eventList) {
		//Items
		List<StockpileItem> stockpileItems = new ArrayList<>();

		updateOwners();

		contractItems.clear();
		assets.clear();
		marketOrders.clear();
		industryJobs.clear();
		transactions.clear();

		//Update Stockpiles (StockpileItem)
		for (Stockpile stockpile : StockpileTab.getShownStockpiles(profileManager)) {
			stockpile.updateDynamicValues();
			stockpileItems.addAll(stockpile.getItems());
			updateStockpile(stockpile);
		}
		//Update Subpiles (SubpileItem)
		stockpileItems.addAll(getUpdatedSubpiles());
		//Update EventList (GUI)
		try {
			eventList.getReadWriteLock().writeLock().lock();
			eventList.clear();
			eventList.addAll(stockpileItems);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}
	}

	public void updateOwners() {
		//Owners Look-Up
		ownersName = new HashMap<>();
		for (OwnerType owner : profileManager.getOwnerTypes()) {
			ownersName.put(owner.getOwnerID(), owner.getOwnerName());
		}
	}

	public void updateStockpile(Stockpile stockpile) {
		//Update owner name
		Set<String> owners = new HashSet<>();
		for (StockpileFilter filter : stockpile.getFilters()) {
			for (Long ownerID : filter.getOwnerIDs()) {
				String owner = ownersName.get(ownerID);
				if (owner != null) {
					owners.add(owner);
				}
			}
		}
		stockpile.setOwnerName(new ArrayList<>(owners));
		//Update Item flag name
		Set<ItemFlag> flags = new HashSet<>();
		for (StockpileFilter filter : stockpile.getFilters()) {
			for (Integer flagID : filter.getFlagIDs()) {
				ItemFlag flag = StaticData.get().getItemFlags().get(flagID);
				if (flag != null) {
					flags.add(flag);
				}
			}
		}
	//Create lookup set of TypeIDs
		Set<Integer> typeIDs = new HashSet<>();
		for (StockpileItem item : stockpile.getItems()) {
			typeIDs.add(item.getItemTypeID());
		}
		addTypeIDs(typeIDs, stockpile);
	//Create lookup maps of Items
		if (!typeIDs.isEmpty()) {
			//Contract Items
			if (stockpile.isContracts()) {
				for (MyContractItem contractItem : profileData.getContractItemList()) {
					if (contractItem.getContract().isIgnoreContract()) {
						continue;
					}
					Integer typeID = contractItem.getTypeID();
					//Ignore null and wrong typeID
					if (typeID == null || !typeIDs.contains(typeID)) {
						continue;
					}
					//BPC has negative value
					if (contractItem.isBPC()) {
						typeID = -typeID;
					}
					//Add Contract Item
					add(contractItems, typeID, contractItem);
				}
			}
			//Assets
			if (stockpile.isAssets()) {
				for (MyAsset asset : profileData.getAssetsList()) {
					if (asset.isGenerated()) { //Skip generated assets
						continue;
					}
					Integer typeID = asset.getTypeID();
					//Ignore null and wrong typeID
					if (typeID == null || !typeIDs.contains(typeID)) {
						continue;
					}
					//BPC has negative value
					if (asset.isBPC()) {
						typeID = -typeID;
					}
					//Add Asset
					add(assets, typeID, asset);
				}
			}
			//Market Orders
			if (stockpile.isBuyOrders() || stockpile.isSellOrders()) {
				for (MyMarketOrder marketOrder : profileData.getMarketOrdersList()) {
					Integer typeID = marketOrder.getTypeID();
					//Ignore null and wrong typeID
					if (typeID == null || !typeIDs.contains(typeID)) {
						continue;
					}
					//Add Market Order
					add(marketOrders, typeID, marketOrder);
				}
			}
			//Industry Jobs
			if (stockpile.isJobs()) {
				for (MyIndustryJob industryJob : profileData.getIndustryJobsList()) {
					//Manufacturing
					Integer productTypeID = industryJob.getProductTypeID();
					if (productTypeID != null && typeIDs.contains(productTypeID)) {
						add(industryJobs, productTypeID, industryJob);
					}
					//Copying
					Integer blueprintTypeID = industryJob.getBlueprintTypeID();
					if (blueprintTypeID != null && typeIDs.contains(blueprintTypeID)) {
						blueprintTypeID = -blueprintTypeID; //Negative - match blueprints copies
						add(industryJobs, blueprintTypeID, industryJob);
					}
				}
			}
			//Transactions
			if (stockpile.isTransactions()) {
				for (MyTransaction transaction : profileData.getTransactionsList()) {
					Integer typeID = transaction.getTypeID();
					//Ignore null and wrong typeID
					if (typeID == null || !typeIDs.contains(typeID)) {
						continue;
					}
					//Add Transaction
					add(transactions, typeID, transaction);
				}
			}
		}
		stockpile.setFlagName(flags);
		stockpile.reset();
		if (!stockpile.isEmpty()) {
			for (StockpileItem item : stockpile.getItems()) {
				if (item instanceof StockpileTotal) {
					continue;
				}
				updateItem(item, stockpile);
			}
		}
		stockpile.updateTotal();
		stockpile.updateTags();
	}

	private <T> void add(Map<Integer, Set<T>> map, Integer typeID, T t) {
		if (typeID == null) {
			return; //Ignore null (should never happen: better safe than sorry)
		}
		Set<T> items = map.get(typeID);
		if (items == null) {
			items = new HashSet<>();
			map.put(typeID, items);
		}
		items.add(t);
	}

	private void addTypeIDs(Set<Integer> typeIDs, Stockpile stockpile) {
		for (StockpileItem item : stockpile.getItems()) {
			typeIDs.add(item.getItemTypeID());
		}
		for (Stockpile subpile : stockpile.getSubpiles().keySet()) {
			addTypeIDs(typeIDs, subpile);
		}
	}

	private void updateItem(StockpileItem item, Stockpile stockpile) {
		final int TYPE_ID = item.getItemTypeID();
		double price = ApiIdConverter.getPrice(TYPE_ID, item.isBPC(), item);
		float volume = ApiIdConverter.getVolume(item.getItem(), true);
		Double transactionAveragePrice = profileData.getTransactionAveragePrice(TYPE_ID);
		item.updateValues(price, volume, transactionAveragePrice);
		//Contract Items
		if (stockpile.isContracts()) {
			Set<MyContractItem> items = contractItems.get(TYPE_ID);
			if (items != null) {
				for (MyContractItem contractItem : items) {
					item.updateContract(contractItem);
				}
			}
		}
		//Assets
		if (stockpile.isAssets()) {
			Set<MyAsset> items = assets.get(TYPE_ID);
			if (items != null) {
				for (MyAsset asset : items) {
					item.updateAsset(asset);
				}
			}
		}
		//Market Orders
		if (stockpile.isBuyOrders() || stockpile.isSellOrders()) {
			Set<MyMarketOrder> items = marketOrders.get(TYPE_ID);
			if (items != null) {
				for (MyMarketOrder marketOrder : items) {
					item.updateMarketOrder(marketOrder);
				}
			}
		}
		//Industry Job
		if (stockpile.isJobs()) {
			Set<MyIndustryJob> items = industryJobs.get(TYPE_ID);
			if (items != null) {
				for (MyIndustryJob industryJob : items) {
					item.updateIndustryJob(industryJob);
				}
			}
		}
		//Transactions
		if (stockpile.isTransactions()) {
			Set<MyTransaction> items = transactions.get(TYPE_ID);
			if (items != null) {
				for (MyTransaction transaction : items) {
					item.updateTransaction(transaction);
				}
			}
		}
	}

	/**
	 * Update Subpiles for all stockpiles and return a list of the updated Subpiles.
	 * This method does not change the EventList
	 * @return
	 */
	private List<StockpileItem> getUpdatedSubpiles() {
		List<StockpileItem> added = new ArrayList<>();
		for (Stockpile stockpile : Settings.get().getStockpiles()) {
			updateSubpile(added, null, stockpile);
		}
		return added;
	}

	/**
	 * Update Subpiles for a single stockpile.
	 * This method will update the EventList (remove old, add new)
	 * This method is very ineffective when updating multiple Stockpiles:
	 * Use getUpdatedSubpiles() to update all
	 * And updateSubpile(,,) for anything > 1
	 * @param eventList
	 * @param parent
	 */
	public void updateSubpile(EventList<StockpileItem> eventList, Stockpile parent) {
		List<StockpileItem> updated = new ArrayList<>();
		List<StockpileItem> removed = new ArrayList<>();
		updateSubpile(updated, removed, parent);
		//Update list
		try {
			eventList.getReadWriteLock().writeLock().lock();
			eventList.removeAll(removed);
			eventList.addAll(updated);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}
	}

	/**
	 * Internal: Don't use this.
	 * Update subpiles for a single stockpile. Does not modify the EventList.
	 * @param updated Updated SubpileItem's
	 * @param removed Removed SubpileItem's
	 * @param parent
	 */
	private void updateSubpile(List<StockpileItem> updated, List<StockpileItem> removed, Stockpile parent) {
		Map<Integer, StockpileItem> parentItems = new HashMap<>();
		for (StockpileItem item : parent.getItems()) {
			parentItems.put(item.getItemTypeID(), item);
		}
		//Save old items (for them to be removed)
		List<SubpileItem> subpileItems = new ArrayList<>(parent.getSubpileItems());
		//Clear old items
		parent.getSubpileItems().clear();
		for (SubpileItem subpileItem : subpileItems) {
			subpileItem.clearItemLinks();
		}
		//Update subs
		for (Stockpile stockpile : parent.getSubpileLinks()) {
			updateSubpile(updated, removed, stockpile);
		}
		//Add new items
		updateSubpile(parent, parent, parentItems, null, 0, "");
		//Update items
		for (SubpileItem subpileItem : parent.getSubpileItems()) {
			updateItem(subpileItem, subpileItem.getStockpile());
		}
		parent.updateTotal();
		//Update lists
		if (removed != null) {
			removed.addAll(subpileItems);
		}
		updated.removeAll(subpileItems);
		if (profileManager.getStockpileIDs().isShown(parent.getId())) {
			updated.addAll(parent.getSubpileItems());
		}
	}

	/**
	 * Internal: Don't use this.
	 * Do all the subpile calculations
	 * (this where the magic happens, 100% certified unreadable code! As required for all critical parts of this software)
	 * @param topStockpile
	 * @param parentStockpile
	 * @param topItems
	 * @param parentStock
	 * @param parentLevel
	 * @param parentPath
	 */
	private void updateSubpile(Stockpile topStockpile, Stockpile parentStockpile, Map<Integer, StockpileItem> topItems, SubpileStock parentStock, int parentLevel, String parentPath) {
		for (Map.Entry<Stockpile, Double> entry : parentStockpile.getSubpiles().entrySet()) {
			//For each subpile (stockpile)
			Stockpile currentStockpile = entry.getKey();
			Double value = entry.getValue();
			String path = parentPath + currentStockpile.getName() + "\r\n";
			int level = parentLevel + 1;
			SubpileStock subpileStock = new SubpileStock(topStockpile, currentStockpile, parentStockpile, parentStock, value, parentLevel, path);
			topStockpile.getSubpileItems().add(subpileStock);
			for (StockpileItem stockpileItem : currentStockpile.getItems()) {
				//For each StockpileItem
				if (stockpileItem.getTypeID() != 0) {
					StockpileItem parentItem = topItems.get(stockpileItem.getItemTypeID());
					SubpileItem subpileItem = new SubpileItem(topStockpile, stockpileItem, subpileStock, parentLevel, path);
					int linkIndex = topStockpile.getSubpileItems().indexOf(subpileItem);
					if (parentItem != null) { //Add link (Advanced: Item + Link)
						subpileItem.addItemLink(parentItem, null); //Add link
					}
					if (linkIndex >= 0) { //Update item (Advanced: Link + Link = MultiLink)
						SubpileItem linkItem = topStockpile.getSubpileItems().get(linkIndex);
						linkItem.addItemLink(stockpileItem, subpileStock);
						if (level >= linkItem.getLevel()) {
							linkItem.setPath(path);
							linkItem.setLevel(level);
						}
					} else { //Add new item (Simple)
						topStockpile.getSubpileItems().add(subpileItem);
					}
				}
			}
			updateSubpile(topStockpile, currentStockpile, topItems, subpileStock, level, path);
		}
	}
}
