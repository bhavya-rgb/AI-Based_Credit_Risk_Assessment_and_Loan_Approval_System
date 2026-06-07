package com.creditrisk.ml.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CsvUtils {
    private CsvUtils() {}

    public static CSVParser open(Path path) throws IOException {
        Reader reader = Files.newBufferedReader(path);
        return CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).build().parse(reader);
    }
}
