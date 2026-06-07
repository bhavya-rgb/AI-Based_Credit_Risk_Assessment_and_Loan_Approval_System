package com.creditrisk.ml.dataset.bootstrap;

import com.creditrisk.ml.preprocessing.LendingClubFeaturePreprocessor;
import com.creditrisk.ml.preprocessing.PreparedDataset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LendingClubSyntheticDatasetGeneratorTest {

    private final LendingClubSyntheticDatasetGenerator generator = new LendingClubSyntheticDatasetGenerator();
    private final LendingClubFeaturePreprocessor preprocessor = new LendingClubFeaturePreprocessor();

    @TempDir
    Path tempDir;

    @Test
    void generatesDeterministicCsvForSameSeedAndRows() throws Exception {
        Path file1 = tempDir.resolve("a.csv");
        Path file2 = tempDir.resolve("b.csv");

        generator.generate(file1, 200, 42L);
        generator.generate(file2, 200, 42L);

        assertThat(Files.exists(file1)).isTrue();
        assertThat(Files.exists(file2)).isTrue();
        assertThat(Files.readString(file1)).isEqualTo(Files.readString(file2));

        long lines = Files.lines(file1).count();
        assertThat(lines).isEqualTo(201); // header + 200 rows
    }

    @Test
    void generatedCsvIsCompatibleWithLendingClubPreprocessor() {
        Path dataset = tempDir.resolve("lending_club_synthetic.csv");
        generator.generate(dataset, 400, 123L);

        PreparedDataset prepared = preprocessor.preprocess(dataset);

        assertThat(prepared.rowsRead()).isEqualTo(400);
        assertThat(prepared.rowsLoaded()).isGreaterThan(0);
        assertThat(prepared.labelDistribution().keySet()).contains(0, 1);
        assertThat(prepared.originalColumns()).contains("loan_status", "issue_d", "earliest_cr_line");
        assertThat(prepared.droppedColumns()).contains("last_pymnt_amnt", "last_pymnt_d", "total_rec_int", "addr_state", "zip_code");
        assertThat(prepared.selectedColumns()).containsAll(List.of("grade", "sub_grade", "purpose", "loan_amnt", "annual_inc", "dti"));
    }
}
