package com.example.gradox2.service.interfaces;

import com.example.gradox2.persistence.entities.GlobalConfig;

public interface IGlobalConfigService {

    GlobalConfig getConfig();

    void reloadConfig();

    GlobalConfig updateConfig(Integer quorumRequired, Double approvalThreshold, Integer maxPendingUploads);
}