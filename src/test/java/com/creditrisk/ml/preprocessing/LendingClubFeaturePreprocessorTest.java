package com.creditrisk.ml.preprocessing;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LendingClubFeaturePreprocessorTest {

    private final LendingClubFeaturePreprocessor preprocessor = new LendingClubFeaturePreprocessor();

    @Test
    void mapsLoanStatusToLabelsPerPaper() {
        assertEquals(0, preprocessor.mapLoanStatusLabel("Current"));
        assertEquals(0, preprocessor.mapLoanStatusLabel("Fully Paid"));
        assertEquals(1, preprocessor.mapLoanStatusLabel("Charged Off"));
        assertEquals(1, preprocessor.mapLoanStatusLabel("Late (16-30 days)"));
        assertNull(preprocessor.mapLoanStatusLabel("Issued"));
    }

    @Test
    void exposesPaperCategoricalMappings() {
        assertEquals(0, preprocessor.encodeCategorical("grade", "A"));
        assertEquals(6, preprocessor.encodeCategorical("grade", "G"));
        assertEquals(24, preprocessor.encodeCategorical("sub_grade", "C4"));
        assertEquals(2, preprocessor.encodeCategorical("verification_status", "Verified"));
        assertEquals(0, preprocessor.encodeCategorical("term", "36 months"));
        assertEquals(10, preprocessor.encodeCategorical("emp_length", "10+ years"));
    }

    @Test
    void parsesOriginationSafeMonthYearDatesToMonthDiff() {
        Optional<Integer> months = preprocessor.parseMonthYearToMonthsSince("Jan-2020", LocalDate.of(2025, 1, 1));
        assertTrue(months.isPresent());
        assertEquals(60, months.get());
        assertTrue(preprocessor.parseMonthYearToMonthsSince("bad", LocalDate.now()).isEmpty());
    }

    @Test
    void preprocessDropsLeakageAndBuildsSchema() throws Exception {
        Path csv = Files.createTempFile("lending-club-mini", ".csv");
        Files.writeString(csv, String.join("\n",
                "id,loan_status,loan_amnt,last_pymnt_amnt,issue_d,earliest_cr_line,dti,member_id,url,zip_code,emp_title,title,addr_state,purpose",
                "1,Current,10000,230,Jan-2020,Jan-2015,0.20,11,http://x,123xx,Engineer,Loan,CA,debt_consolidation",
                "2,Charged Off,5000,0,Feb-2020,May-2012,0.45,12,http://y,999xx,Analyst,Loan,TX,credit_card"
        ));

        PreparedDataset dataset = preprocessor.preprocess(csv);
        assertEquals(2, dataset.rowsRead());
        assertEquals(2, dataset.rowsLoaded());
        assertTrue(dataset.droppedColumns().contains("last_pymnt_amnt"));
        assertTrue(dataset.droppedColumns().contains("member_id"));
        assertTrue(dataset.droppedColumns().contains("url"));
        assertTrue(dataset.selectedColumns().contains("dti"));
        assertTrue(dataset.preprocessingSchema().containsKey("categoricalMappings"));
        assertEquals(1L, dataset.labelDistribution().get(0));
        assertEquals(1L, dataset.labelDistribution().get(1));
    }
}
