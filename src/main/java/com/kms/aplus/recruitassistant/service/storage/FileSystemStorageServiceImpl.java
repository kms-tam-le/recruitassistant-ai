package com.kms.aplus.recruitassistant.service.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.kms.aplus.recruitassistant.exception.FileNotFoundException;
import com.kms.aplus.recruitassistant.exception.StorageException;
import com.kms.aplus.recruitassistant.properties.StorageProperties;


@Service
public class FileSystemStorageServiceImpl implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                .filter(path -> !path.equals(this.rootLocation))
                .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new FileNotFoundException(
                        "Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void classifier() throws IOException{
        String modelLoc = "D:\\recruitAi\\RecruitAssistantAIProject\\upload-dir\\ner-model.ser.gz";
        CRFClassifier model = CRFClassifier.getClassifierNoExceptions(modelLoc);
        File location = new File("D:\\recruitAi\\RecruitAssistantAIProject\\upload-dir\\");
        String type = ".txt";
        String testfiles[] = new String[10];
        int p = 0;
        if (location.isDirectory() && location != null) {
            for (File f : location.listFiles()) {
                if (f.isFile() && f.getName().endsWith(type)) {
                    testfiles[p++] = f.getName();
                }
            }
        }
        int index = 0;
        for (int i = 0; i < 1; i++) {
            File file = new File("D:\\recruitAi\\RecruitAssistantAIProject\\upload-dir\\" + testfiles[i]);
            String txt = FileUtils.readFileToString(file, "UTF-8");
            doTagging(model, txt, index);
            index++;
        }
    }

    private void doTagging(CRFClassifier model, String input, int index) throws IOException {
        input = input.trim();
        String output = model.classifyToString(input);
        String lines[] = output.split("\n");
        String text[] = new String[lines.length];
        String label[] = new String[lines.length];
        String entities[] = new String[]{"Name", "College Name", "Degree", "Graduation Year", "Years of Experience",
                "Companies worked at", "Designation", "Skills", "Location", "Email Address"};
        //Whatever the file path is.

        FileWriter w = new FileWriter("D:\\recruitAi\\RecruitAssistantAIProject\\result\\" + Integer.toString(index) + ".txt", true);

        for (String entity : entities) {
            int f = 0;
            for (String line : lines) {
                String tokens[] = line.split(" ");
                for (String token : tokens) {
                    String s[] = token.split("/");
                    if (s.length == 2 && s[1].equals(entity) && f == 1)
                        w.write(s[0] + " ");
                    if (s.length == 2 && s[1].equals(entity) && f == 0) {
                        w.write(entity + ":\n" + s[0] + " ");
                        f = 1;
                    }
                }
            }
            w.write("\n");
        }
        w.close();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
