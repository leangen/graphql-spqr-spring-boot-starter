package io.leangen.graphql.spqr.spring.web;

import io.leangen.graphql.spqr.spring.autoconfigure.SpqrProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class GuiController {

    private final SpqrProperties config;

    public GuiController(SpqrProperties config) {
        this.config = config;
    }

    @ResponseBody
    @RequestMapping(value = "${graphql.spqr.gui.endpoint:/gui}", produces = "text/html; charset=utf-8")
    public String gui() throws IOException {
        String playgroundHtml = StreamUtils.copyToString(new ClassPathResource("playground.html")
                .getInputStream(), StandardCharsets.UTF_8)
                .replace("${pageTitle}", config.getGui().getPageTitle())
                .replace("${graphQLEndpoint}", config.getGui().getTargetEndpoint())
                .replace("${webSocketEndpoint}", config.getGui().getTargetWsEndpoint());

        return config.getGui().isOffline() ? playgroundHtml.replace("//", "/")
                : playgroundHtml;
    }
}
