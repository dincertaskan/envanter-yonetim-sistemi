package com.inventory;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uygulamanın tamamını (güvenlik + controller + servis + H2 veritabanı) birlikte test eder.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Oturum açmanız gerekiyor"));
    }

    @Test
    void registeredUserGetsUserRoleAndCannotCreateEquipment() throws Exception {
        String token = register("ayse@test.com", "5551112233");

        mockMvc.perform(get("/api/equipment").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(equipmentJson("SN-USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        register("mehmet@test.com", "5552223344");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("mehmet@test.com", "5559998877")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Kaya","email":"gecersiz","phone":"12","password":"zayif"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com","password":"YanlisSifre1!"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateEquipmentAndItIsAudited() throws Exception {
        String adminToken = login("admin@test.com", "Admin123!");

        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(equipmentJson("SN-ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SN-ADMIN"))
                .andExpect(jsonPath("$.createdBy").isNotEmpty());

        mockMvc.perform(get("/api/audit-logs").param("action", "CREATE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.equipmentName == 'Osiloskop')]").exists());
    }

    // --- yardımcı metotlar ---

    private String register(String email, String phone) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, phone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private static String registerJson(String email, String phone) {
        return """
                {"firstName":"Test","lastName":"Kullanıcı","email":"%s","phone":"%s","password":"Sifre123!"}
                """.formatted(email, phone);
    }

    private static String equipmentJson(String serialNumber) {
        return """
                {"name":"Osiloskop","brand":"Rigol","model":"DS1054Z","serialNumber":"%s","calibrationDate":"2026-05-01"}
                """.formatted(serialNumber);
    }
}
