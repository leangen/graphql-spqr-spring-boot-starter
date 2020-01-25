package io.leangen.graphql.spqr.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "graphql.spqr")
@SuppressWarnings("WeakerAccess")
public class SpqrProperties {

    public static String DEFAULT_ENDPOINT = "/graphql";
    public static String DEFAULT_GUI_ENDPOINT = "/gui";

    // Core properties
    private String[] basePackages;
    private boolean abstractInputTypeResolution;
    private Relay relay = new Relay();

    // Web properties
    private Http http = new Http();
    private WebSocket ws = new WebSocket();

    // GUI properties
    private Gui gui = new Gui();

    public SpqrProperties() {
    }

    @PostConstruct
    public void setDefaults() {
        if (StringUtils.isEmpty(ws.getEndpoint())) {
            ws.setEndpoint(http.endpoint);
        }
        if (StringUtils.isEmpty(gui.getTargetEndpoint())) {
            gui.setTargetEndpoint(http.endpoint);
        }
        if (StringUtils.isEmpty(gui.getTargetWsEndpoint())) {
            gui.setTargetWsEndpoint(ws.endpoint);
        }
    }

    public String[] getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

    public boolean isAbstractInputTypeResolution() {
        return abstractInputTypeResolution;
    }

    public void setAbstractInputTypeResolution(boolean abstractInputTypeResolution) {
        this.abstractInputTypeResolution = abstractInputTypeResolution;
    }

    public Relay getRelay() {
        return relay;
    }

    public void setRelay(Relay relay) {
        this.relay = relay;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public WebSocket getWs() {
        return ws;
    }

    public void setWs(WebSocket ws) {
        this.ws = ws;
    }

    public Gui getGui() {
        return gui;
    }

    public void setGui(Gui gui) {
        this.gui = gui;
    }

    public static class Relay {

        private boolean enabled;
        private String mutationWrapper;
        private String mutationWrapperDescription;
        private boolean connectionCheckRelaxed;
        private boolean springDataCompatible;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMutationWrapper() {
            return mutationWrapper;
        }

        public void setMutationWrapper(String mutationWrapper) {
            this.mutationWrapper = mutationWrapper;
        }

        public String getMutationWrapperDescription() {
            return mutationWrapperDescription;
        }

        public void setMutationWrapperDescription(String mutationWrapperDescription) {
            this.mutationWrapperDescription = mutationWrapperDescription;
        }

        public boolean isConnectionCheckRelaxed() {
            return connectionCheckRelaxed;
        }

        public void setConnectionCheckRelaxed(boolean connectionCheckRelaxed) {
            this.connectionCheckRelaxed = connectionCheckRelaxed;
        }

        public boolean isSpringDataCompatible() {
            return springDataCompatible;
        }

        public void setSpringDataCompatible(boolean springDataCompatible) {
            this.springDataCompatible = springDataCompatible;
        }
    }

    public static class Http {

        private boolean enabled = true;
        private String endpoint = DEFAULT_ENDPOINT;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public static class WebSocket {

        private boolean enabled = true;
        private String endpoint;
        private String[] allowedOrigins = new String[] {"*"};
        private KeepAlive keepAlive = new KeepAlive();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String[] getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String[] allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public KeepAlive getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(KeepAlive keepAlive) {
            this.keepAlive = keepAlive;
        }

        public static class KeepAlive {

            private boolean enabled;
            private int intervalMillis = 10000;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getIntervalMillis() {
                return intervalMillis;
            }

            public void setIntervalMillis(int intervalMillis) {
                this.intervalMillis = intervalMillis;
            }
        }
    }

    public static class Gui {

        private boolean enabled = true;
        private String endpoint = DEFAULT_GUI_ENDPOINT;
        private String targetEndpoint;
        private String targetWsEndpoint;
        private String pageTitle = "GraphQL Playground";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getTargetEndpoint() {
            return targetEndpoint;
        }

        public void setTargetEndpoint(String targetEndpoint) {
            this.targetEndpoint = targetEndpoint;
        }

        public String getTargetWsEndpoint() {
            return targetWsEndpoint;
        }

        public void setTargetWsEndpoint(String targetWsEndpoint) {
            this.targetWsEndpoint = targetWsEndpoint;
        }

        public String getPageTitle() {
            return pageTitle;
        }

        public void setPageTitle(String pageTitle) {
            this.pageTitle = pageTitle;
        }
    }
}
