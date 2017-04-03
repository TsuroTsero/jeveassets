/*
 * Copyright 2009-2016 Contributors (see credits.txt)
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
package net.nikr.eve.jeveasset.io.evekit;


import enterprises.orbital.evekit.client.invoker.ApiClient;
import enterprises.orbital.evekit.client.invoker.ApiException;
import enterprises.orbital.evekit.client.model.Blueprint;
import java.util.Date;
import java.util.List;
import net.nikr.eve.jeveasset.data.evekit.EveKitAccessMask;
import net.nikr.eve.jeveasset.data.evekit.EveKitOwner;
import net.nikr.eve.jeveasset.gui.dialogs.update.UpdateTask;


public class EveKitBlueprintsGetter extends AbstractEveKitListGetter<Blueprint> {

	@Override
	public void load(UpdateTask updateTask, List<EveKitOwner> owners) {
		super.load(updateTask, owners);
	}

	@Override
	protected List<Blueprint> get(EveKitOwner owner, Long contid) throws ApiException {
		return getCommonApi().getBlueprints(owner.getAccessKey(), owner.getAccessCred(), null, contid, MAX_RESULTS, REVERSE,
				null, null, null, null, null, null, null, null, null);
	}

	@Override
	protected void set(EveKitOwner owner, List<Blueprint> data) throws ApiException {
		owner.setBlueprints(EveKitConverter.convertBlueprints(data));
	}

	@Override
	protected long getCID(Blueprint obj) {
		return obj.getCid();
	}

	@Override
	protected boolean isNow(Blueprint obj) {
		return obj.getLifeEnd() == Long.MAX_VALUE;
	}

	@Override
	protected String getTaskName() {
		return "Blueprints";
	}

	@Override
	protected long getAccessMask() {
		return EveKitAccessMask.BLUEPRINTS.getAccessMask();
	}

	@Override
	protected void setNextUpdate(EveKitOwner owner, Date date) {
		owner.setBlueprintsNextUpdate(date);
	}

	@Override
	protected ApiClient getApiClient() {
		return getCommonApi().getApiClient();
	}

	@Override
	protected void saveCID(EveKitOwner owner, Long cid) { } //Always get all data

	@Override
	protected Long loadCID(EveKitOwner owner) {
		return null; //Always get all data
	}
}
