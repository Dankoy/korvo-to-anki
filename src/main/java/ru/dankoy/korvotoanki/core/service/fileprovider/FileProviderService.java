package ru.dankoy.korvotoanki.core.service.fileprovider;

import java.nio.file.Path;

public interface FileProviderService {

  Path provide(String name);
}
