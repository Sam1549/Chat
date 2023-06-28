package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Logger {
    private static Logger instance;

    private Logger() {
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(String mes) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter("./log.txt", true))) {
            printWriter.write(mes + "\n");
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
