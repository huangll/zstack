package org.zstack.network.securitygroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.cloudbus.CloudBusCallBack;
import org.zstack.header.core.Completion;
import org.zstack.header.core.NoErrorCompletion;
import org.zstack.header.message.MessageReply;
import org.zstack.header.network.l3.L3NetworkInventory;
import org.zstack.header.network.service.NetworkServiceProviderType;
import org.zstack.header.network.service.NetworkServiceType;
import org.zstack.header.vm.*;
import org.zstack.network.service.AbstractNetworkServiceExtension;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

/**
 */
public class SecurityGroupNetworkServiceExtension extends AbstractNetworkServiceExtension {
    private static final CLogger logger = Utils.getLogger(SecurityGroupNetworkServiceExtension.class);

    @Autowired
    private CloudBus bus;

    @Override
    public NetworkServiceExtensionPosition getNetworkServiceExtensionPosition() {
        return NetworkServiceExtensionPosition.AFTER_VM_CREATED;
    }

    @Override
    public NetworkServiceType getNetworkServiceType() {
        return SecurityGroupProviderFactory.networkServiceType;
    }

    @Override
    public void applyNetworkService(VmInstanceSpec servedVm, Map<String, Object> data, final Completion completion) {
        Map<NetworkServiceProviderType, List<L3NetworkInventory>> map = getNetworkServiceProviderMap(SecurityGroupProviderFactory.networkServiceType,
                VmNicSpec.getL3NetworkInventoryOfSpec(servedVm.getL3Networks()));
        if (map.isEmpty()) {
            completion.success();
            return;
        }

        RefreshSecurityGroupRulesOnVmMsg msg = new RefreshSecurityGroupRulesOnVmMsg();
        msg.setVmInstanceUuid(servedVm.getVmInventory().getUuid());
        msg.setHostUuid(servedVm.getDestHost().getUuid());
        bus.makeLocalServiceId(msg, SecurityGroupConstant.SERVICE_ID);
        bus.send(msg, new CloudBusCallBack(completion) {
            @Override
            public void run(MessageReply reply) {
                if (reply.isSuccess()) {
                    completion.success();
                } else {
                    completion.fail(reply.getError());
                }
            }
        });
    }

    @Override
    public void releaseNetworkService(final VmInstanceSpec servedVm, Map<String, Object> data, final NoErrorCompletion completion) {
        RefreshSecurityGroupRulesOnVmMsg msg = new RefreshSecurityGroupRulesOnVmMsg();
        msg.setVmInstanceUuid(servedVm.getVmInventory().getUuid());
        msg.setHostUuid(servedVm.getDestHost().getUuid());
        msg.setDeleteAllRules(true);
        bus.makeLocalServiceId(msg, SecurityGroupConstant.SERVICE_ID);
        bus.send(msg, new CloudBusCallBack(completion) {
            @Override
            public void run(MessageReply reply) {
                if (!reply.isSuccess()) {
                    logger.debug(String.format("failed to remove security group rules for vm[uuid:%s], %s", servedVm.getVmInventory().getUuid(), reply.getError()));
                }

	            if (servedVm.getCurrentVmOperation() == VmInstanceConstant.VmOperation.DetachNic) {
		            VmNicInventory nic = servedVm.getDestNics().get(0);
		            deleteVmNicSecurityGroupRef(nic.getUuid());
	            }

                completion.done();
            }
        });
    }

    @Transactional
    private void deleteVmNicSecurityGroupRef(String vmNicUuid){
	    String sql = String.format("delete from %s ref where ref.%s = :id",
			    VmNicSecurityGroupRefVO.class.getSimpleName(), "vmNicUuid");
	    Query query = dbf.getEntityManager().createQuery(sql);
	    query.setParameter("id", vmNicUuid);
	    query.executeUpdate();
    }
}
