package com.example.simactivation;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Admin@123";
        String hash = encoder.encode(password);

        System.out.println("Generated Hash: " + hash);
        System.out.println("Hash Length: " + hash.length());
        System.out.println("Hash Ends With: " + hash.substring(hash.length() - 5));
        System.out.println("Password Match: "
                + encoder.matches(password, hash));
    }
}
