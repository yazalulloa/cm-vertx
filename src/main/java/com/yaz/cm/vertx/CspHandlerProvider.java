package com.yaz.cm.vertx;

import com.yaz.cm.vertx.domain.constants.GoogleUrls;
import io.vertx.ext.web.handler.CSPHandler;

public class CspHandlerProvider {

  private static final String CSP_STYLE_SRC = "style-src";
  private static final String CSP_STYLE_SRC_ELEM = "style-src-elem";
  private static final String CSP_FONT_SRC = "font-src";


  public CSPHandler cspHandler() {

    return CSPHandler.create()
        .addDirective("default-src", "self")
        .addDirective(CSP_STYLE_SRC, "self")
        .addDirective(CSP_STYLE_SRC, "'sha256-d7rFBVhb3n/Drrf+EpNWYdITkos3kQRFpB0oSOycXg4='")
        .addDirective(CSP_STYLE_SRC, "'sha256-jPRu7by0R+2ETbOf5tPoWVYj5vtouMdYPT7unU4YjBY='")
        .addDirective(CSP_STYLE_SRC, "'sha256-4Su6mBWzEIFnH4pAGMOuaeBrstwJN4Z3pq/s1Kn4/KQ='")
        .addDirective(CSP_STYLE_SRC_ELEM, "self")
        .addDirective(CSP_STYLE_SRC_ELEM, GoogleUrls.ACCOUNTS)
        .addDirective(CSP_STYLE_SRC_ELEM, GoogleUrls.FONTS)
        .addDirective(CSP_STYLE_SRC_ELEM, "'sha256-d7rFBVhb3n/Drrf+EpNWYdITkos3kQRFpB0oSOycXg4='")
        .addDirective(CSP_STYLE_SRC_ELEM, "'sha256-lmto2U1o7YINyHPg9TOCjIt+o5pSFNU/T2oLxDPF+uw='")
        .addDirective(CSP_STYLE_SRC_ELEM, "'sha256-PA7DEE0Iz6UflFZhpJephshhktZb3j6+NxvSfpf574M='")
        .addDirective(CSP_FONT_SRC, "self")
        .addDirective(CSP_FONT_SRC, GoogleUrls.FONTS)
        .addDirective(CSP_FONT_SRC, GoogleUrls.FONTS_GSTATIC)
        .addDirective("connect-src", "self")
        .addDirective("connect-src", GoogleUrls.ACCOUNTS)
        .addDirective("img-src", "self")
        .addDirective("img-src", GoogleUrls.ACCOUNTS)
        .addDirective("script-src-elem", "self")
        .addDirective("script-src-elem", GoogleUrls.ACCOUNTS)
        .addDirective("frame-src", "self")
        .addDirective("frame-src", GoogleUrls.ACCOUNTS);
  }

}
