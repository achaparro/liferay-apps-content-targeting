/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.content.targeting.service.persistence;

import com.liferay.content.targeting.model.Campaign;
import com.liferay.content.targeting.model.impl.CampaignImpl;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.permission.InlineSQLHelperUtil;
import com.liferay.portal.service.persistence.impl.BasePersistenceImpl;
import com.liferay.util.dao.orm.CustomSQLUtil;

import java.util.Date;
import java.util.List;

/**
 * @author Eduardo Garcia
 */
public class CampaignFinderImpl
	extends BasePersistenceImpl<Campaign> implements CampaignFinder {

	public static final String FIND_BY_G_D_A_U =
		CampaignFinder.class.getName() + ".findByG_D_A_U";

	@Override
	public Campaign fetchByG_D_A_U_First(
			long[] groupIds, Date date, boolean active, long[] userSegmentIds)
		throws SystemException {

		return doFetchByG_D_A_U_First(
			groupIds, date, active, userSegmentIds, false);
	}

	@Override
	public List<Campaign> filterFindByG_D_A_U(
			long[] groupIds, Date date, boolean active, long[] userSegmentIds)
		throws SystemException {

		return doFetchByG_D_A_U(groupIds, date, active, userSegmentIds, true);
	}

	@Override
	public List<Campaign> findByG_D_A_U(
			long[] groupIds, Date date, boolean active, long[] userSegmentIds)
		throws SystemException {

		return doFetchByG_D_A_U(groupIds, date, active, userSegmentIds, false);
	}

	@Override
	public Campaign filterFetchByG_D_A_U_First(
			long[] groupIds, Date date, boolean active, long[] userSegmentIds)
		throws SystemException {

		return doFetchByG_D_A_U_First(
			groupIds, date, active, userSegmentIds, true);
	}

	protected List<Campaign> doFetchByG_D_A_U(
			long[] groupIds, Date date, boolean active, long[] userSegmentIds,
			boolean inlineSQLHelper)
		throws SystemException {

		if ((userSegmentIds == null) || (userSegmentIds.length == 0)) {
			userSegmentIds = new long[] {-1};
		}

		Session session = null;

		try {
			session = openSession();

			CustomSQLUtil.reloadCustomSQL();

			String sql = CustomSQLUtil.get(FIND_BY_G_D_A_U);

			sql = StringUtil.replace(
				sql, "[$GROUP_ID$]", getGroupIds(groupIds));
			sql = StringUtil.replace(
				sql, "[$USER_SEGMENT_IDS$]", StringUtil.merge(userSegmentIds));

			if (inlineSQLHelper) {
				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, Campaign.class.getName(), "CT_Campaign.campaignId",
					groupIds);
			}

			SQLQuery q = session.createSQLQuery(sql);

			q.addEntity("Campaign", CampaignImpl.class);

			QueryPos qPos = QueryPos.getInstance(q);

			qPos.add(groupIds);
			qPos.add(date);
			qPos.add(date);
			qPos.add(active);

			return q.list();
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected Campaign doFetchByG_D_A_U_First(
			long[] groupIds, Date date, boolean active, long[] userSegmentIds,
			boolean inlineSQLHelper)
		throws SystemException {

		List<Campaign> campaigns = doFetchByG_D_A_U(
			groupIds, date, active, userSegmentIds, inlineSQLHelper);

		if ((campaigns != null) && !campaigns.isEmpty()) {
			return campaigns.get(0);
		}

		return null;
	}

	protected String getGroupIds(long[] groupIds) {
		if (groupIds.length == 0) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(groupIds.length * 2);

		sb.append(StringPool.OPEN_PARENTHESIS);

		for (int i = 0; i < groupIds.length; i++) {
			sb.append("groupId = ?");

			if ((i + 1) < groupIds.length) {
				sb.append(" OR ");
			}
		}

		sb.append(") AND");

		return sb.toString();
	}

}