package com.kms.aplus.recruitassistant.controller;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.kms.aplus.recruitassistant.service.storage.StorageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileUploadControllerIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@MockBean
	private StorageService storageService;

	@LocalServerPort
	private int port;
	
	private static final String TEST_FILE_NAME = "test.txt";
	private static final String TEST_FILE_PATH = "/"+ TEST_FILE_NAME;

	@Test
	public void shouldUploadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource(TEST_FILE_PATH, getClass());

		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("file", resource);
		ResponseEntity<String> response = this.restTemplate.postForEntity("/", map,
				String.class);

		assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.FOUND);
		assertThat(response.getHeaders().getLocation().toString())
				.startsWith("http://localhost:" + this.port + "/");
		then(storageService).should().store(any(MultipartFile.class));
	}

	@Test
	public void shouldDownloadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource(TEST_FILE_PATH, getClass());
		given(this.storageService.loadAsResource(TEST_FILE_NAME)).willReturn(resource);

		ResponseEntity<String> response = this.restTemplate
				.getForEntity("/files/{filename}", String.class, TEST_FILE_NAME);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
				.isEqualTo("attachment; filename=\""+ TEST_FILE_NAME +"\"");
		assertThat(response.getBody()).isEqualTo("Recruit AI");
	}

}
