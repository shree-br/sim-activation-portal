package com.example.simactivation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
                <html>
                <head>
                    <title>SIM Activation Portal</title>
                </head>
                <body style="font-family: Arial; text-align: center; margin-top: 100px;">
                    <h1>Welcome to SIM Activation Portal</h1>
                    <p>SIM Activation Portal is running successfully!</p>
                    <button onclick="alert('Welcome!')">Get Started</button>
                </body>
                </html>
                """;
    }
}