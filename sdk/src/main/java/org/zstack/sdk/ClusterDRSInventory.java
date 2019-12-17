package org.zstack.sdk;

import org.zstack.sdk.DRSState;
import org.zstack.sdk.DRSAutomationLevel;

public class ClusterDRSInventory  {

    public java.lang.String clusterUuid;
    public void setClusterUuid(java.lang.String clusterUuid) {
        this.clusterUuid = clusterUuid;
    }
    public java.lang.String getClusterUuid() {
        return this.clusterUuid;
    }

    public DRSState state;
    public void setState(DRSState state) {
        this.state = state;
    }
    public DRSState getState() {
        return this.state;
    }

    public DRSAutomationLevel automationLevel;
    public void setAutomationLevel(DRSAutomationLevel automationLevel) {
        this.automationLevel = automationLevel;
    }
    public DRSAutomationLevel getAutomationLevel() {
        return this.automationLevel;
    }

    public java.lang.String thresholds;
    public void setThresholds(java.lang.String thresholds) {
        this.thresholds = thresholds;
    }
    public java.lang.String getThresholds() {
        return this.thresholds;
    }

    public java.lang.Integer thresholdDuration;
    public void setThresholdDuration(java.lang.Integer thresholdDuration) {
        this.thresholdDuration = thresholdDuration;
    }
    public java.lang.Integer getThresholdDuration() {
        return this.thresholdDuration;
    }

    public java.lang.String description;
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    public java.lang.String getDescription() {
        return this.description;
    }

    public java.sql.Timestamp createDate;
    public void setCreateDate(java.sql.Timestamp createDate) {
        this.createDate = createDate;
    }
    public java.sql.Timestamp getCreateDate() {
        return this.createDate;
    }

    public java.sql.Timestamp lastOpDate;
    public void setLastOpDate(java.sql.Timestamp lastOpDate) {
        this.lastOpDate = lastOpDate;
    }
    public java.sql.Timestamp getLastOpDate() {
        return this.lastOpDate;
    }

    public java.lang.String uuid;
    public void setUuid(java.lang.String uuid) {
        this.uuid = uuid;
    }
    public java.lang.String getUuid() {
        return this.uuid;
    }

    public java.lang.String name;
    public void setName(java.lang.String name) {
        this.name = name;
    }
    public java.lang.String getName() {
        return this.name;
    }

}
