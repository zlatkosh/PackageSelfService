package com.zlatko.packageselfservicebackend;

import org.springframework.boot.SpringApplication;

public class TestPackageSelfServiceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(PackageSelfServiceBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
