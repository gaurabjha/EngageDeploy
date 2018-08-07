package com.omi.bean;

import java.util.Comparator;

public class DC implements Comparable {
	private int dcid;
	private String serverName;
	private String siteName;

	private String patchGroupId;

	public DC(int dcid, String serverName, String siteName, String patchGroupId) {
		super();
		this.dcid = dcid;
		this.serverName = serverName;
		this.siteName = siteName;
		this.patchGroupId = patchGroupId;
	}

	public DC() {

	}

	public String getPatchGroupId() {
		return patchGroupId;
	}

	public void setPatchGroupId(String patchGroupId) {
		this.patchGroupId = patchGroupId;
	}

	public int getDcid() {
		return dcid;
	}

	public void setDcid(int dcid) {
		this.dcid = dcid;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	@Override
	public String toString() {

		return String
				.format(String.format("    %1$-11s %4$-10s %2$-29s %3$s", dcid, serverName, siteName, patchGroupId));

	}

	@Override
	public int compareTo(Object o) {
		return getDcid() - ((DC) o).getDcid();
	}

}

