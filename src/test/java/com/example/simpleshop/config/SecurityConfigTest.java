package com.example.simpleshop.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessingSwaggerUI_thenSuccess() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingH2Console_thenSuccess() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingSecuredEndpoint_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAccessingPublicEndpoint_thenSuccess() throws Exception {
        // Test signup endpoint
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"signup@example.com\"," +
                                "\"password\":\"password123\"," +
                                "\"nickname\":\"tester\"}"))
                .andExpect(status().isOk());

        // Test login endpoint
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"alice@example.com\"," +
                                "\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/users/login")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().string("Access-Control-Allow-Headers", "content-type"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
    
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder options(String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(url);
    }
}


