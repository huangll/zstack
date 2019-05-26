package org.zstack.sdk;

public enum PciDeviceVirtStatus {
	UNVIRTUALIZABLE,
	SRIOV_VIRTUALIZABLE,
	VFIO_MDEV_VIRTUALIZABLE,
	SRIOV_VIRTUALIZED,
	VFIO_MDEV_VIRTUALIZED,
	SRIOV_VIRTUAL,
	UNKNOWN,
}