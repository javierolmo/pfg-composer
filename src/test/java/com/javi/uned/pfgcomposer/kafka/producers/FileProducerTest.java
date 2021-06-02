package com.javi.uned.pfgcomposer.kafka.producers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@SpringBootTest
class FileProducerTest {

    @Autowired
    FileProducer fileProducer;

    @Test
    public void test() throws IOException {
        File file = new File("./Dockerfile");
        fileProducer.sendXML("39", file);
    }

}