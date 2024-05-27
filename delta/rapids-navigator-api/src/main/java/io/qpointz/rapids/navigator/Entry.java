package io.qpointz.rapids.navigator;

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Entry {

    public static void main(String[] args) {
        val ctx = SpringApplication.run(Entry.class,args);
    }

}
