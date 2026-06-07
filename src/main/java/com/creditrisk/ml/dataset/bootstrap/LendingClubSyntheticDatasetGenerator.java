package com.creditrisk.ml.dataset.bootstrap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class LendingClubSyntheticDatasetGenerator {
    private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);

    private static final List<String> HEADERS = List.of(
            "loan_status",
            "grade",
            "sub_grade",
            "verification_status",
            "purpose",
            "term",
            "initial_list_status",
            "emp_length",
            "home_ownership",
            "application_type",
            "loan_amnt",
            "annual_inc",
            "dti",
            "delinq_2yrs",
            "inq_last_6mths",
            "open_acc",
            "pub_rec",
            "revol_bal",
            "revol_util",
            "total_acc",
            "mort_acc",
            "pub_rec_bankruptcies",
            "issue_d",
            "earliest_cr_line",
            "installment",
            "int_rate",
            "fico_range_low",
            "fico_range_high",
            "last_pymnt_amnt",
            "last_pymnt_d",
            "total_rec_int",
            "addr_state",
            "zip_code"
    );

    private static final List<String> PURPOSES = List.of(
            "debt_consolidation", "credit_card", "home_improvement", "other", "major_purchase",
            "medical", "house", "car", "vacation", "small_business", "moving", "renewable_energy", "wedding"
    );
    private static final List<String> VERIFICATION_STATUSES = List.of("Not Verified", "Source Verified", "Verified");
    private static final List<String> HOME_OWNERSHIP = List.of("MORTGAGE", "RENT", "OWN", "ANY");
    private static final List<String> APPLICATION_TYPES = List.of("INDIVIDUAL", "INDIVIDUAL", "JOINT");
    private static final List<String> EMP_LENGTHS = buildEmpLengths();
    private static final List<String> STATES = List.of("CA", "TX", "FL", "NY", "WA", "NJ", "IL", "GA", "NC", "AZ");
    private static final List<String> GOOD_STATUSES = List.of("Current", "Fully Paid");
    private static final List<String> BAD_STATUSES = List.of("Charged Off", "Default", "Late (31-120 days)", "Late (16-30 days)", "In Grace Period");

    public Path generate(Path outputPath, int rows, long seed) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null");
        }
        if (rows <= 0) {
            throw new IllegalArgumentException("rows must be > 0");
        }
        Path path = outputPath.toAbsolutePath().normalize();
        try {
            Files.createDirectories(path.getParent() == null ? path.toAbsolutePath().getParent() : path.getParent());
            try (Writer writer = Files.newBufferedWriter(path);
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(HEADERS.toArray(String[]::new)).build())) {
                Random random = new Random(seed);
                for (int i = 0; i < rows; i++) {
                    printer.printRecord(generateRow(i, random));
                }
            }
            return path;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate synthetic dataset: " + e.getMessage(), e);
        }
    }

    private List<String> generateRow(int rowNum, Random random) {
        List<String> row = new ArrayList<>(HEADERS.size());
        String grade = weightedGrade(random);
        String subGrade = grade + (1 + random.nextInt(5));
        String verificationStatus = VERIFICATION_STATUSES.get(random.nextInt(VERIFICATION_STATUSES.size()));
        String purpose = PURPOSES.get(random.nextInt(PURPOSES.size()));
        String term = random.nextDouble() < 0.72 ? "36 months" : "60 months";
        String initialListStatus = random.nextBoolean() ? "w" : "f";
        String empLength = EMP_LENGTHS.get(random.nextInt(EMP_LENGTHS.size()));
        String homeOwnership = HOME_OWNERSHIP.get(random.nextInt(HOME_OWNERSHIP.size()));
        String applicationType = APPLICATION_TYPES.get(random.nextInt(APPLICATION_TYPES.size()));

        double loanAmount = round2(1000 + random.nextInt(39000));
        double annualIncome = round2(18000 + random.nextDouble() * 220000);
        double dti = round4(Math.min(0.75, Math.max(0.0, random.nextGaussian() * 0.08 + 0.18)));
        int delinq2yrs = boundedInt((int) Math.round(Math.abs(random.nextGaussian() * 1.2)), 0, 8);
        int inqLast6Mths = boundedInt((int) Math.round(Math.abs(random.nextGaussian() * 2.0 + 1.5)), 0, 12);
        int openAcc = boundedInt((int) Math.round(Math.abs(random.nextGaussian() * 6 + 10)), 1, 40);
        int pubRec = boundedInt((int) Math.round(Math.abs(random.nextGaussian() * 0.7)), 0, 5);
        double revolBal = round2(Math.abs(random.nextGaussian() * 15000 + 12000));
        double revolUtil = round4(Math.min(99.9, Math.max(0.0, random.nextGaussian() * 18 + 48)));
        int totalAcc = boundedInt(openAcc + random.nextInt(30), openAcc, 90);
        int mortAcc = boundedInt((int) Math.round(Math.abs(random.nextGaussian() * 2.5 + 2)), 0, 15);
        int bankruptcies = random.nextDouble() < 0.92 ? 0 : (random.nextDouble() < 0.85 ? 1 : 2);

        LocalDate issueDate = randomMonthYear(random, 2014, 2022);
        int monthsBack = 12 + random.nextInt(360);
        LocalDate earliestCrLine = issueDate.minusMonths(monthsBack);

        double intRate = round4(5.5 + random.nextDouble() * 22.0);
        double installment = round2((loanAmount * (1 + (intRate / 100.0) * ("60 months".equals(term) ? 1.8 : 0.9)))
                / ("60 months".equals(term) ? 60.0 : 36.0));
        int ficoLow = 580 + random.nextInt(251);
        int ficoHigh = Math.min(850, ficoLow + 4);

        String loanStatus = sampleLoanStatus(random, dti, delinq2yrs, inqLast6Mths, bankruptcies, ficoLow);
        double lastPymntAmt = round2(Math.max(0.0, installment + random.nextGaussian() * 20));
        LocalDate lastPymntDate = issueDate.plusMonths(Math.min("60 months".equals(term) ? 59 : 35, random.nextInt(18)));
        double totalRecInt = round2(Math.max(0.0, loanAmount * (intRate / 100.0) * (random.nextDouble() * 0.6)));
        String addrState = STATES.get(random.nextInt(STATES.size()));
        String zipCode = String.format(Locale.US, "%05d", 10000 + random.nextInt(89999)).substring(0, 3) + "xx";

        row.add(loanStatus);
        row.add(grade);
        row.add(subGrade);
        row.add(verificationStatus);
        row.add(purpose);
        row.add(term);
        row.add(initialListStatus);
        row.add(empLength);
        row.add(homeOwnership);
        row.add(applicationType);
        row.add(num2(loanAmount));
        row.add(num2(annualIncome));
        row.add(num4(dti));
        row.add(String.valueOf(delinq2yrs));
        row.add(String.valueOf(inqLast6Mths));
        row.add(String.valueOf(openAcc));
        row.add(String.valueOf(pubRec));
        row.add(num2(revolBal));
        row.add(num4(revolUtil));
        row.add(String.valueOf(totalAcc));
        row.add(String.valueOf(mortAcc));
        row.add(String.valueOf(bankruptcies));
        row.add(MONTH_YEAR.format(issueDate));
        row.add(MONTH_YEAR.format(earliestCrLine));
        row.add(num2(installment));
        row.add(num4(intRate));
        row.add(String.valueOf(ficoLow));
        row.add(String.valueOf(ficoHigh));
        row.add(num2(lastPymntAmt));
        row.add(MONTH_YEAR.format(lastPymntDate));
        row.add(num2(totalRecInt));
        row.add(addrState);
        row.add(zipCode);
        return row;
    }

    private String sampleLoanStatus(Random random, double dti, int delinq2yrs, int inq6, int bankruptcies, int ficoLow) {
        double risk = 0.0;
        risk += dti > 0.35 ? 0.18 : 0.0;
        risk += delinq2yrs * 0.04;
        risk += inq6 > 4 ? 0.06 : 0.0;
        risk += bankruptcies * 0.16;
        risk += ficoLow < 640 ? 0.18 : (ficoLow < 680 ? 0.07 : 0.0);
        risk += random.nextDouble() * 0.18;
        if (risk > 0.42) {
            return BAD_STATUSES.get(random.nextInt(BAD_STATUSES.size()));
        }
        return GOOD_STATUSES.get(random.nextInt(GOOD_STATUSES.size()));
    }

    private String weightedGrade(Random random) {
        double x = random.nextDouble();
        if (x < 0.20) return "A";
        if (x < 0.42) return "B";
        if (x < 0.64) return "C";
        if (x < 0.80) return "D";
        if (x < 0.91) return "E";
        if (x < 0.97) return "F";
        return "G";
    }

    private LocalDate randomMonthYear(Random random, int startYearInclusive, int endYearInclusive) {
        int year = startYearInclusive + random.nextInt((endYearInclusive - startYearInclusive) + 1);
        Month month = Month.of(1 + random.nextInt(12));
        return LocalDate.of(year, month, 1);
    }

    private int boundedInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static List<String> buildEmpLengths() {
        List<String> out = new ArrayList<>();
        out.add("< 1 year");
        out.add("1 year");
        for (int i = 2; i <= 9; i++) {
            out.add(i + " years");
        }
        out.add("10+ years");
        return out;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double round4(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }

    private String num2(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private String num4(double v) {
        return String.format(Locale.US, "%.4f", v);
    }
}
