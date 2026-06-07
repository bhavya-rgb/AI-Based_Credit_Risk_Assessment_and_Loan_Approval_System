package com.creditrisk.ml.preprocessing;

import com.creditrisk.common.ApiException;
import com.creditrisk.ml.common.CsvUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LendingClubFeaturePreprocessor implements FeaturePreprocessor {

    private static final Set<String> IRRELEVANT_OR_LEAKAGE_COLUMNS = Set.of(
            "id", "member_id", "url", "zip_code", "emp_title", "title", "addr_state",
            "last_pymnt_amnt", "last_pymnt_d", "next_pymnt_d", "total_rec_prncp", "total_rec_int",
            "total_rec_late_fee", "out_prncp", "out_prncp_inv",
            "recoveries", "collection_recovery_fee", "last_credit_pull_d"
    );

    private static final Set<String> APPROVAL_ALLOWED_DATE_COLUMNS = Set.of("issue_d", "earliest_cr_line");

    @Override
    public PreparedDataset preprocess(Path csvPath) {
        if (csvPath == null || !java.nio.file.Files.exists(csvPath)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Dataset file not found: " + csvPath);
        }

        try (CSVParser parser = CsvUtils.open(csvPath)) {
            List<String> headers = parser.getHeaderNames();
            Map<String, Long> missingCounts = new LinkedHashMap<>();
            headers.forEach(h -> missingCounts.put(h, 0L));
            Map<Integer, Long> labelDistribution = new LinkedHashMap<>();
            long rowsRead = 0;
            long rowsLoaded = 0;

            for (CSVRecord record : parser) {
                rowsRead++;
                boolean rowUsable = true;
                for (String h : headers) {
                    String v = get(record, h);
                    if (v == null || v.isBlank() || "NA".equalsIgnoreCase(v) || "null".equalsIgnoreCase(v)) {
                        missingCounts.computeIfPresent(h, (k, old) -> old + 1);
                    }
                }
                Integer label = mapLoanStatusLabel(get(record, "loan_status"));
                if (label != null) {
                    labelDistribution.merge(label, 1L, Long::sum);
                    rowsLoaded++;
                } else {
                    rowUsable = false;
                }
                if (!rowUsable) {
                    // keep reading to compute stats but do not count as loaded
                }
            }

            final long totalRows = rowsRead;
            List<String> droppedByMissing = headers.stream()
                    .filter(h -> totalRows > 0 && missingCounts.getOrDefault(h, 0L) > totalRows / 2)
                    .toList();

            List<String> dropped = new ArrayList<>();
            dropped.addAll(droppedByMissing);

            for (String h : headers) {
                String lower = h.toLowerCase(Locale.ROOT);
                boolean exactLeakage = IRRELEVANT_OR_LEAKAGE_COLUMNS.contains(lower);
                boolean totalRecWildcard = lower.startsWith("total_rec_");
                boolean repaymentStatusField = lower.contains("pymnt") && !APPROVAL_ALLOWED_DATE_COLUMNS.contains(lower);
                boolean forbiddenDate = lower.endsWith("_d") && !APPROVAL_ALLOWED_DATE_COLUMNS.contains(lower) && !"issue_d".equals(lower) && !"earliest_cr_line".equals(lower);
                if (exactLeakage || totalRecWildcard || repaymentStatusField || forbiddenDate) {
                    if (!dropped.contains(h)) dropped.add(h);
                }
            }

            List<String> selected = headers.stream()
                    .filter(h -> !dropped.contains(h))
                    .filter(h -> !"loan_status".equalsIgnoreCase(h))
                    .toList();

            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("categoricalMappings", paperCategoricalMappings());
            schema.put("labelMapping", Map.of(
                    "Current", 0,
                    "Fully Paid", 0,
                    "In Grace Period", 1,
                    "Late (31-120 days)", 1,
                    "Late (16-30 days)", 1,
                    "Charged Off", 1,
                    "Default", 1,
                    "Late (31–120 days)", 1,
                    "Late (16–30 days)", 1
            ));
            schema.put("missingValuePolicy", Map.of("continuous", "median", "categorical", "mode", "dropThreshold", ">50%"));
            schema.put("normalization", "min-max");
            schema.put("approvalAllowedDateColumns", APPROVAL_ALLOWED_DATE_COLUMNS);
            schema.put("droppedColumns", dropped);

            String datasetVersion = deriveDatasetVersion(csvPath);
            return new PreparedDataset(
                    datasetVersion,
                    csvPath.toAbsolutePath().toString(),
                    rowsRead,
                    rowsLoaded,
                    headers,
                    selected,
                    dropped,
                    missingCounts,
                    labelDistribution,
                    schema
            );
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read dataset: " + e.getMessage());
        }
    }

    public Integer mapLoanStatusLabel(String loanStatus) {
        if (loanStatus == null) return null;
        String normalized = loanStatus.trim();
        if (normalized.isEmpty()) return null;
        return switch (normalized) {
            case "Current", "Fully Paid" -> 0;
            case "In Grace Period", "Late (31-120 days)", "Late (16-30 days)", "Charged Off", "Default",
                 "Late (31–120 days)", "Late (16–30 days)" -> 1;
            default -> null;
        };
    }

    public Map<String, Map<String, Integer>> paperCategoricalMappings() {
        Map<String, Map<String, Integer>> mappings = new LinkedHashMap<>();
        mappings.put("grade", mapOfSequence(List.of("A", "B", "C", "D", "E", "F", "G"), 0, 1));

        Map<String, Integer> subGrade = new LinkedHashMap<>();
        for (int g = 0; g < 7; g++) {
            char grade = (char) ('A' + g);
            int base = g == 0 ? 0 : g * 10 + 1;
            for (int n = 1; n <= 5; n++) {
                int value = (g == 0 ? n : base + (n - 1));
                subGrade.put("" + grade + n, value);
            }
        }
        mappings.put("sub_grade", subGrade);
        mappings.put("verification_status", Map.of("Not Verified", 0, "Source Verified", 1, "Source-Verified", 1, "Verified", 2));
        mappings.put("term", Map.of("36 months", 0, "60 months", 1));
        mappings.put("initial_list_status", Map.of("w", 0, "f", 1));
        mappings.put("home_ownership", Map.of("MORTGAGE", 0, "RENT", 1, "OWN", 2, "ANY", 3, "NONE", 4));
        mappings.put("application_type", Map.of("INDIVIDUAL", 0, "JOINT", 1, "JOINT APP", 1));
        mappings.put("emp_length", empLengthMapping());
        mappings.put("purpose", purposeMapping());
        return mappings;
    }

    public int encodeCategorical(String field, String value) {
        if (value == null) return -1;
        Map<String, Integer> mapping = paperCategoricalMappings().get(field);
        if (mapping == null) return -1;
        return mapping.getOrDefault(value, mapping.getOrDefault(value.trim(), -1));
    }

    public Optional<Integer> parseMonthYearToMonthsSince(String raw, LocalDate referenceDate) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
            LocalDate date = LocalDate.parse("01-" + raw.replace(" ", ""), DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));
            int months = (referenceDate.getYear() - date.getYear()) * 12 + (referenceDate.getMonthValue() - date.getMonthValue());
            return Optional.of(Math.max(months, 0));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    private String get(CSVRecord record, String header) {
        try {
            return record.get(header);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Map<String, Integer> mapOfSequence(List<String> values, int start, int step) {
        Map<String, Integer> out = new LinkedHashMap<>();
        int idx = start;
        for (String v : values) {
            out.put(v, idx);
            idx += step;
        }
        return out;
    }

    private Map<String, Integer> empLengthMapping() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("< 1 year", 0);
        map.put("<1 year", 0);
        for (int i = 1; i <= 9; i++) {
            map.put(i + " years", i);
            map.put(i + " year", i);
        }
        map.put("10+ years", 10);
        return map;
    }

    private Map<String, Integer> purposeMapping() {
        List<String> ordered = List.of(
                "debt_consolidation", "credit_card", "home_improvement", "other", "major_purchase", "medical",
                "house", "car", "vacation", "small_business", "moving", "renewable_energy", "wedding"
        );
        return ordered.stream().collect(Collectors.toMap(v -> v, ordered::indexOf, (a, b) -> a, LinkedHashMap::new));
    }

    private String deriveDatasetVersion(Path csvPath) {
        String base = csvPath.getFileName().toString().replaceAll("\\.[^.]+$", "");
        return "LC_" + java.time.LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + base.replaceAll("[^A-Za-z0-9]+", "_");
    }
}
