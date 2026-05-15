package com.casbytes.core.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTML landing page at {@code /} with product summary, Maven build version/time, and JVM uptime.
 */
@RestController
@RequiredArgsConstructor
public class RootController {

  private final RootInfoPageService rootInfoPageService;

  @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
  public String root() {
    return rootInfoPageService.renderHtml();
  }
}
