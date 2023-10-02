package ru.dankoy.korvotoanki.core.service.io;

import java.io.IOException;

public interface IOService {

  void print(String message);

  String readAllLines() throws IOException;

  String readLn();

  long readLong();
}
