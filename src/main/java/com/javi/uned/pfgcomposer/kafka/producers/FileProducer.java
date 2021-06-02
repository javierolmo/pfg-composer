package com.javi.uned.pfgcomposer.kafka.producers;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@Service
public class FileProducer {

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;


    public void sendXML(String key, File xmlFile) throws IOException {
        byte[] rawFile = FileUtils.readFileToByteArray(xmlFile);
        kafkaTemplate.send("melodia.composer.xml", key, rawFile);
    }

    public void sendPDF(String key, File pdfFile) throws IOException {
        byte[] rawFile = FileUtils.readFileToByteArray(pdfFile);
        kafkaTemplate.send("melodia.composer.pdf", key, rawFile);
    }



}
