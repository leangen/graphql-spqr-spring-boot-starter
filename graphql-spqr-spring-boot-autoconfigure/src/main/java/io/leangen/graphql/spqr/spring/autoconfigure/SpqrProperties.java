package io.leangen.graphql.spqr.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "graphql.spqr")
public class SpqrProperties {

    private String[] queryBasePackages;
    private boolean relaySupported;
    private String relayMutationWrapper;
    private String relayMutationWrapperDescription;

    public SpqrProperties() {
    }

    public String[] getQueryBasePackages() {
        return queryBasePackages;
    }

    public void setQueryBasePackages(String[] queryBasePackages) {
        this.queryBasePackages = queryBasePackages;
    }

    public boolean getRelaySupported() {
        return relaySupported;
    }

    public void setRelaySupported(boolean relaySupported) {
        this.relaySupported = relaySupported;
    }

    public String getRelayMutationWrapper() {
        return relayMutationWrapper;
    }

    public void setRelayMutationWrapper(String relayMutationWrapper) {
        this.relayMutationWrapper = relayMutationWrapper;
    }

    public String getRelayMutationWrapperDescription() {
        return relayMutationWrapperDescription;
    }

    public void setRelayMutationWrapperDescription(String relayMutationWrapperDescription) {
        this.relayMutationWrapperDescription = relayMutationWrapperDescription;
    }
}
