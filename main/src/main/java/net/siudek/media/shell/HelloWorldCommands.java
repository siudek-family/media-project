package net.siudek.media.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloWorldCommands {

    @ShellMethod(value = "Says hello to the world", key = "hello")
    public String hello() {
        return "Hello World from Spring Shell!";
    }

    @ShellMethod(value = "Greets a person by name", key = "greet")
    public String greet(@ShellOption(defaultValue = "User") String name) {
        return String.format("Hello, %s! Welcome to Spring Shell.", name);
    }

    @ShellMethod(value = "Adds two numbers", key = "add")
    public String add(int a, int b) {
        return String.format("%d + %d = %d", a, b, a + b);
    }
}
