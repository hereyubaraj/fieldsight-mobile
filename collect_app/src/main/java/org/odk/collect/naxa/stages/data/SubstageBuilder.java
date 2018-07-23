package org.odk.collect.naxa.stages.data;
import org.odk.collect.naxa.generalforms.data.Em;
import org.odk.collect.naxa.generalforms.data.FormResponse;

import java.util.List;

public class SubstageBuilder {
    private Integer id;
    private StageForms stageForms;
    private String name;
    private String description;
    private Integer order;
    private String responseCount;
    private Em em;
    private Integer projectStageId;
    private List<FormResponse> latestSubmission;

    public SubstageBuilder setId(Integer id) {
        this.id = id;
        return this;
    }

    public SubstageBuilder setStageForms(StageForms stageForms) {
        this.stageForms = stageForms;
        return this;
    }

    public SubstageBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SubstageBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public SubstageBuilder setOrder(Integer order) {
        this.order = order;
        return this;
    }

    public SubstageBuilder setResponseCount(String responseCount) {
        this.responseCount = responseCount;
        return this;
    }

    public SubstageBuilder setEm(Em em) {
        this.em = em;
        return this;
    }

    public SubstageBuilder setProjectStageId(Integer projectStageId) {
        this.projectStageId = projectStageId;
        return this;
    }

    public SubstageBuilder setLatestSubmission(List<FormResponse> latestSubmission) {
        this.latestSubmission = latestSubmission;
        return this;
    }

    public Substage createSubstageNEWAPI() {
        return new Substage(id, stageForms, name, description, order, responseCount, em, projectStageId, latestSubmission);
    }
}