package com.redhat.service.smartevents.manager.v2.services;

import com.redhat.service.smartevents.manager.v2.api.user.models.requests.BridgeRequest;
import com.redhat.service.smartevents.manager.v2.api.user.models.responses.BridgeResponse;
import com.redhat.service.smartevents.manager.v2.persistence.models.Bridge;

public interface BridgeService {

    Bridge createBridge(String customerId, String organisationId, String owner, BridgeRequest bridgeRequest);

    BridgeResponse toResponse(Bridge bridge);
}