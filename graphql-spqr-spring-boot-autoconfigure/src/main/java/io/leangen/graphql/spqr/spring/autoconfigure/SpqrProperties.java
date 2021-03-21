package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.util.Utils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "graphql.spqr")
@SuppressWarnings("WeakerAccess")
public class SpqrProperties {

    public static String DEFAULT_ENDPOINT = "/graphql";
    public static String DEFAULT_GUI_ENDPOINT = "/gui";

    // Core properties
    private String[] basePackages;
    private boolean abstractInputTypeResolution;
    private int maxComplexity = -1;
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
        if (Utils.isEmpty(ws.getEndpoint())) {
            ws.setEndpoint(http.endpoint);
        }
        if (Utils.isEmpty(gui.getTargetEndpoint())) {
            gui.setTargetEndpoint(http.endpoint);
        }
        if (Utils.isEmpty(gui.getTargetWsEndpoint())) {
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

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public void setMaxComplexity(int maxComplexity) {
        this.maxComplexity = maxComplexity;
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
        private Mvc mvc = new Mvc();

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

        public Mvc getMvc() {
            return mvc;
        }

        public void setMvc(Mvc mvc) {
            this.mvc = mvc;
        }

        public static class Mvc {
            private Executor executor = Executor.ASYNC;

            public Executor getExecutor() {
                return executor;
            }

            public void setExecutor(Executor executor) {
                this.executor = executor;
            }

            public enum Executor {
                ASYNC, BLOCKING
            }
        }
    }

    public static class WebSocket {

        private boolean enabled = true;
        private String endpoint;
        private int sendTimeLimit = 10 * 1000;
        private int sendBufferSizeLimit = 512 * 1024;
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

        public int getSendTimeLimit() {
            return sendTimeLimit;
        }

        public void setSendTimeLimit(int sendTimeLimit) {
            this.sendTimeLimit = sendTimeLimit;
        }

        public int getSendBufferSizeLimit() {
            return sendBufferSizeLimit;
        }

        public void setSendBufferSizeLimit(int sendBufferSizeLimit) {
            this.sendBufferSizeLimit = sendBufferSizeLimit;
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
