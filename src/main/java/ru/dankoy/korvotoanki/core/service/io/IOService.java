package ru.dankoy.korvotoanki.core.service.io;

public interface IOService {

  void print(String message);

  String readAllLines();

  String readLn();

  long readLong();
}
