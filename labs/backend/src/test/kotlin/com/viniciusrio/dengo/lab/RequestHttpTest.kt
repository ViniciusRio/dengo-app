package com.viniciusrio.dengo.lab

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RequestHttpTest(@param:Autowired private val mvc: MockMvc) {
    @Test fun startsWithEmptyListAndCreatesPendingRequest() {
        mvc.perform(get("/lab/requests"))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))

        mvc.perform(post("/lab/requests").contentType(MediaType.APPLICATION_JSON).content("""{"type":"DENGO"}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.type").value("DENGO"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.createdAt").isString)
    }

    @Test fun repeatedTypeReturnsExistingRequestWithOk() {
        val command = post("/lab/requests").contentType(MediaType.APPLICATION_JSON).content("""{"type":"DENGO"}""")

        mvc.perform(command).andExpect(status().isCreated).andExpect(jsonPath("$.id").value(1))
        mvc.perform(command).andExpect(status().isOk).andExpect(jsonPath("$.id").value(1))
        mvc.perform(get("/lab/requests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test fun invalidInputDoesNotCreateRequests() {
        for (body in listOf("{}", """{"type":"OTHER"}""", """{"type":"UNKNOWN"}""", """{"type":"DENGO","name":"real person"}""", "{")) {
            mvc.perform(post("/lab/requests").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest)
        }
        mvc.perform(get("/lab/requests"))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))
    }

    @Test fun queriesKeepTwoTypesInCreationOrder() {
        for (type in listOf("DENGO", "MEDICINE")) {
            mvc.perform(post("/lab/requests").contentType(MediaType.APPLICATION_JSON).content("""{"type":"$type"}"""))
                .andExpect(status().isCreated)
        }
        repeat(2) {
            mvc.perform(get("/lab/requests"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("DENGO"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("MEDICINE"))
        }
    }
}
