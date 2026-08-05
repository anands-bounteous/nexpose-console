package com.rapid7.nexpose.console.domain;

import java.util.ArrayList;
import java.util.List;

/** Configuration for a scan request: the targets and the engine/template to use. */
public class ScanConfig {

    private String name;
    private String templateId = "full-audit";
    private String engineName = "Local scan engine";
    private final List<String> targets = new ArrayList<>();   // IPs / CIDRs / hostnames

    public ScanConfig() {
    }

    public ScanConfig(String name, List<String> targets) {
        this.name = name;
        if (targets != null) {
            this.targets.addAll(targets);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getEngineName() { return engineName; }
    public void setEngineName(String engineName) { this.engineName = engineName; }

    public List<String> getTargets() { return targets; }
}
