/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.sbomer.service.test.feature.sbom;

import static io.restassured.RestAssured.given;

import java.util.Arrays;
import java.util.HashSet;

import org.hamcrest.CoreMatchers;
import org.jboss.sbomer.core.features.sbom.enums.GeneratorType;
import org.jboss.sbomer.core.features.sbom.enums.ProcessorType;
import org.jboss.sbomer.service.feature.sbom.model.Sbom;
import org.jboss.sbomer.service.feature.sbom.rest.Page;
import org.jboss.sbomer.service.feature.sbom.service.SbomService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.restassured.http.ContentType;

@QuarkusTest
@WithKubernetesTestServer
public class SBOMResourceRSQLTest {

    @InjectSpy
    SbomService sbomService;

    // @InjectMock
    // GenerationService generationService;

    @Test
    public void testRSQLSearchPagination() {
        // One page, one result
        int pageIndex = 0;
        int pageSizeLarge = 50;
        Page<Sbom> singlePagedOneSbom = initializeOneResultPaginated(pageIndex, pageSizeLarge);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSizeLarge, "buildId=eq=AWI7P3EJ23YAA"))
                .thenReturn(singlePagedOneSbom);

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSizeLarge
                                + "&query=buildId=eq=AWI7P3EJ23YAA")
                .then()
                .statusCode(200)
                .body("pageIndex", CoreMatchers.equalTo(pageIndex))
                .and()
                .body("pageSize", CoreMatchers.equalTo(pageSizeLarge))
                .and()
                .body("totalPages", CoreMatchers.equalTo(1))
                .and()
                .body("totalHits", CoreMatchers.equalTo(1))
                .and()
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        // One page, two results
        Page<Sbom> singlePagedTwoSboms = initializeTwoResultsPaginated(pageIndex, pageSizeLarge);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSizeLarge, "buildId=eq=AWI7P3EJ23YAA"))
                .thenReturn(singlePagedTwoSboms);

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSizeLarge
                                + "&query=buildId=eq=AWI7P3EJ23YAA")
                .then()
                .statusCode(200)
                .body("pageIndex", CoreMatchers.equalTo(pageIndex))
                .and()
                .body("pageSize", CoreMatchers.equalTo(pageSizeLarge))
                .and()
                .body("totalPages", CoreMatchers.equalTo(1))
                .and()
                .body("totalHits", CoreMatchers.equalTo(2))
                .and()
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.id", CoreMatchers.hasItem("54321"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        // Two pages, two results
        int pageSizeTiny = 1;

        Page<Sbom> doublePagedTwoSboms = initializeTwoResultsPaginated(pageIndex, pageSizeTiny);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSizeTiny, "buildId=eq=AWI7P3EJ23YAA"))
                .thenReturn(doublePagedTwoSboms);

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSizeTiny
                                + "&query=buildId=eq=AWI7P3EJ23YAA")
                .then()
                .statusCode(200)
                .body("pageIndex", CoreMatchers.equalTo(pageIndex))
                .and()
                .body("pageSize", CoreMatchers.equalTo(pageSizeTiny))
                .and()
                .body("totalPages", CoreMatchers.equalTo(2))
                .and()
                .body("totalHits", CoreMatchers.equalTo(2))
                .and()
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.id", CoreMatchers.hasItem("54321"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));
    }

    @Test
    public void testRSQLSearch() {
        int pageIndex = 0;
        int pageSize = 50;

        Page<Sbom> pagedSboms = initializeOneResultPaginated(pageIndex, pageSize);

        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "id==12345")).thenReturn(pagedSboms);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "buildId==AWI7P3EJ23YAA"))
                .thenReturn(pagedSboms);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "buildId=eq=AWI7P3EJ23YAA"))
                .thenReturn(pagedSboms);
        Mockito.when(
                sbomService.searchSbomsByQueryPaginated(
                        pageIndex,
                        pageSize,
                        "rootPurl=='pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom'"))
                .thenReturn(pagedSboms);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "statusMessage=='all went well'"))
                .thenReturn(pagedSboms);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "statusMessage=='all*'"))
                .thenReturn(pagedSboms);
        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "statusMessage=='*went*'"))
                .thenReturn(pagedSboms);

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize + "&query=id==12345")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=buildId==AWI7P3EJ23YAA")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=buildId=eq=AWI7P3EJ23YAA")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=rootPurl=='pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom'")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=statusMessage=='all went well'")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=statusMessage=='all*'")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=statusMessage=='*went*'")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));
    }

    @Test
    public void testRSQLSearchNotNullValues() {
        int pageIndex = 0;
        int pageSize = 50;
        Page<Sbom> pagedSboms = initializeOneResultPaginated(pageIndex, pageSize);

        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "rootPurl=isnull=false"))
                .thenReturn(pagedSboms);

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=rootPurl=isnull=false")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));
    }

    @Test
    public void testRSQLSearchNullValues() {
        int pageIndex = 0;
        int pageSize = 50;
        Page<Sbom> pagedSboms = initializeOneResultPaginated(pageIndex, pageSize);
        pagedSboms.getContent().stream().forEach(sbom -> {
            sbom.setRootPurl(null);
        });

        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "rootPurl=isnull=true"))
                .thenReturn(pagedSboms);

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=rootPurl=isnull=true")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

    }

    @Test
    public void testRSQLSearchNotAllowedProperty() {
        String msg = "RSQL on field Sbom.sbom with type JsonNode is not supported!";

        given().when()
                .contentType(ContentType.JSON)
                .request("GET", "/api/v1alpha1/sboms?query=sbom==null")
                .then()
                .statusCode(400)
                .body(CoreMatchers.equalTo(msg));
    }

    @Test
    public void testRSQLSearchAndLogicalNode() {

        int pageIndex = 0;
        int pageSize = 50;

        String BUILD_ID = "AWI7P3EJ23YAA";
        String ROOT_PURL = "pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom";
        Sbom base = createFirstSbom();
        base.setBuildId(BUILD_ID);
        base.setRootPurl(ROOT_PURL);

        Page<Sbom> oneContentPage = initializeOneResultPaginated(1, 1);
        Page<Sbom> twoContentPage = initializeTwoResultsPaginated(1, 2);

        Mockito.when(sbomService.searchSbomsByQueryPaginated(pageIndex, pageSize, "buildId=eq=AWI7P3EJ23YAA"))
                .thenReturn(oneContentPage);
        Mockito.when(
                sbomService.searchSbomsByQueryPaginated(
                        pageIndex,
                        pageSize,
                        "rootPurl=eq='pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom'"))
                .thenReturn(oneContentPage);
        Mockito.when(
                sbomService.searchSbomsByQueryPaginated(
                        pageIndex,
                        pageSize,
                        "buildId=eq=AWI7P3EJ23YAA;rootPurl=eq='pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom'"))
                .thenReturn(oneContentPage);
        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=buildId=eq=AWI7P3EJ23YAA;rootPurl=eq='pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom'")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));

        Mockito.when(
                sbomService.searchSbomsByQueryPaginated(
                        pageIndex,
                        pageSize,
                        "buildId=eq=AWI7P3EJ23YAA,buildId=eq=AWI7P3EJ23YAB"))
                .thenReturn(twoContentPage);
        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=buildId=eq=AWI7P3EJ23YAA,buildId=eq=AWI7P3EJ23YAB")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.id", CoreMatchers.hasItem("54321"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAB"));

        given().when()
                .contentType(ContentType.JSON)
                .request(
                        "GET",
                        "/api/v1alpha1/sboms?pageIndex=" + pageIndex + "&pageSize=" + pageSize
                                + "&query=rootPurl=eq='pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom'")
                .then()
                .statusCode(200)
                .body("content.id", CoreMatchers.hasItem("12345"))
                .and()
                .body("content.buildId", CoreMatchers.hasItem("AWI7P3EJ23YAA"));
    }

    private Page<Sbom> initializeOneResultPaginated(int pageIndex, int pageSize) {
        int totalHits = 1;
        int totalPages = (int) Math.ceil((double) totalHits / (double) pageSize);
        return new Page<Sbom>(pageIndex, pageSize, totalPages, totalHits, Arrays.asList(createFirstSbom()));
    }

    private Page<Sbom> initializeTwoResultsPaginated(int pageIndex, int pageSize) {
        int totalHits = 2;
        int totalPages = (int) Math.ceil((double) totalHits / (double) pageSize);
        return new Page<Sbom>(
                pageIndex,
                pageSize,
                totalPages,
                totalHits,
                Arrays.asList(createFirstSbom(), createSecondSbom()));
    }

    private Sbom createFirstSbom() {
        Sbom sbom = new Sbom();
        sbom.setId("12345");
        sbom.setBuildId("AWI7P3EJ23YAA");
        sbom.setRootPurl("pkg:maven/org.apache.logging.log4j/log4j@2.19.0.redhat-00001?type=pom");
        sbom.setStatusMessage("all went well");
        return sbom;
    }

    private Sbom createSecondSbom() {
        Sbom sbom = new Sbom();
        sbom.setId("54321");
        sbom.setBuildId("AWI7P3EJ23YAB");
        sbom.setRootPurl("pkg:maven/org.apache.logging.log4j/log4j@2.119.0.redhat-00002?type=pom");
        sbom.setStatusMessage("all went well, again");
        return sbom;
    }

}
