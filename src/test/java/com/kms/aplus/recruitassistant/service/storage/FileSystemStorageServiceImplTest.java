package com.kms.aplus.recruitassistant.service.storage;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.kms.aplus.recruitassistant.properties.StorageProperties;
import com.kms.aplus.recruitassistant.service.storage.FileSystemStorageServiceImpl;
import com.kms.aplus.recruitassistant.storage.StorageException;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemStorageServiceImplTest {

    private StorageProperties properties = new StorageProperties();
    private FileSystemStorageServiceImpl service;

    @Before
    public void init() {
        properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
        service = new FileSystemStorageServiceImpl(properties);
        service.init();
    }

    @Test
    public void loadNonExistent() {
        assertThat(service.load("foo.txt")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        service.store(new MockMultipartFile("foo", "foo.txt", MediaType.TEXT_PLAIN_VALUE,
                "Hello World".getBytes()));
        assertThat(service.load("foo.txt")).exists();
    }

    @Test(expected = StorageException.class)
    public void saveNotPermitted() {
        service.store(new MockMultipartFile("foo", "../foo.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello World".getBytes()));
    }

    @Test
    public void savePermitted() {
        service.store(new MockMultipartFile("foo", "bar/../foo.txt",
                MediaType.TEXT_PLAIN_VALUE, "Hello World".getBytes()));
    }

}
