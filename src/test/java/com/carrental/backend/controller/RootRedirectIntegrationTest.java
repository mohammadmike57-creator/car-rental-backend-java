package com.carrental.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-that-is-long-enough-for-hmac-sha256",
    "spring.datasource.url=jdbc:h2:mem:root-it;DB_CLOSE_DELAY=-1",
    "app.base-url=https://www.nctrental.com"
})
@AutoConfigureMockMvc
class RootRedirectIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void browsersAreSentToTheWebsite() throws Exception {
        mvc.perform(get("/").header("Accept", "text/html,application/xhtml+xml").header("Host", "nctrental.com"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "https://www.nctrental.com"));
    }

    @Test
    void healthChecksStillGetOk() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string("OK"));
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string("OK"));
    }

    @Test
    void noRedirectLoopOnTheWebsiteHost() throws Exception {
        mvc.perform(get("/").header("Accept", "text/html").header("Host", "www.nctrental.com"))
            .andExpect(status().isOk());
    }
}
