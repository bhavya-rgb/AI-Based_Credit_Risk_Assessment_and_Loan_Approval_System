package com.creditrisk.ml.scoring;

import com.creditrisk.common.JsonSupport;
import com.creditrisk.loan.CreditProfileSnapshotEntity;
import com.creditrisk.loan.LoanApplicationEntity;
import com.creditrisk.ml.model.MlModelEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

@Service
public class ArtifactHeuristicRiskScorer implements RiskScorer {
    private final JsonSupport jsonSupport;

    public ArtifactHeuristicRiskScorer(JsonSupport jsonSupport) {
        this.jsonSupport = jsonSupport;
    }

    @Override
    public ScoringComputationResult score(MlModelEntity model, LoanApplicationEntity application, CreditProfileSnapshotEntity creditProfile) {
        Map<String, Double> weights = defaultWeights();
        try {
            Path path = Path.of(model.getArtifactPath());
            if (Files.exists(path)) {
                Map<String, Object> artifact = jsonSupport.fromJson(Files.readString(path), new TypeReference<>() {});
                Object payloadObj = artifact.get("payload");
                if (payloadObj instanceof Map<?, ?> payload && payload.get("weights") instanceof Map<?, ?> w) {
                    Map<String, Double> parsed = new LinkedHashMap<>();
                    w.forEach((k, v) -> parsed.put(String.valueOf(k), Double.parseDouble(String.valueOf(v))));
                    if (!parsed.isEmpty()) {
                        weights = parsed;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        Map<String, Double> features = extract(application, creditProfile);
        double linear = 0.0;
        List<Map.Entry<String, Double>> contributions = new ArrayList<>();
        for (Map.Entry<String, Double> e : features.entrySet()) {
            double w = weights.getOrDefault(e.getKey(), 0.0);
            double c = w * e.getValue();
            linear += c;
            contributions.add(Map.entry(e.getKey(), c));
        }
        linear += weights.getOrDefault("bias", -1.2);
        double pd = 1.0 / (1.0 + Math.exp(-linear));
        pd = Math.max(0.001, Math.min(0.999, pd));

        contributions.sort((a, b) -> Double.compare(Math.abs(b.getValue()), Math.abs(a.getValue())));
        List<String> reasons = contributions.stream().limit(5)
                .map(e -> (e.getValue() >= 0 ? "UP_" : "DOWN_") + e.getKey().toUpperCase(Locale.ROOT))
                .toList();

        String hash = sha256Hex(features.toString());
        return new ScoringComputationResult(BigDecimal.valueOf(pd).setScale(6, RoundingMode.HALF_UP), reasons, hash);
    }

    private Map<String, Double> extract(LoanApplicationEntity app, CreditProfileSnapshotEntity cp) {
        Map<String, Double> f = new LinkedHashMap<>();
        f.put("loan_amount_k", safe(app.getLoanAmount()) / 1000.0);
        f.put("annual_income_k", safe(app.getAnnualIncome()) / 1000.0);
        f.put("dti", safe(app.getDti()));
        f.put("existing_debt_k", safe(app.getExistingDebt()) / 1000.0);
        f.put("term_years", app.getTermMonths() == null ? 0.0 : app.getTermMonths() / 12.0);
        f.put("employment_years", app.getEmploymentLengthYears() == null ? 0.0 : app.getEmploymentLengthYears());
        if (cp != null) {
            double ficoAvg = avg(cp.getFicoLow(), cp.getFicoHigh());
            f.put("fico_inverse", ficoAvg <= 0 ? 0.0 : (850.0 - ficoAvg) / 850.0);
            f.put("inq_last_6mths", cp.getInqLast6Mths() == null ? 0.0 : cp.getInqLast6Mths());
            f.put("delinq_2yrs", cp.getDelinq2Yrs() == null ? 0.0 : cp.getDelinq2Yrs());
            f.put("revol_util", safe(cp.getRevolUtil()));
            f.put("bankruptcies", cp.getPubRecBankruptcies() == null ? 0.0 : cp.getPubRecBankruptcies());
        } else {
            f.put("fico_inverse", 0.5);
            f.put("inq_last_6mths", 0.0);
            f.put("delinq_2yrs", 0.0);
            f.put("revol_util", 0.0);
            f.put("bankruptcies", 0.0);
        }
        return f;
    }

    private Map<String, Double> defaultWeights() {
        Map<String, Double> w = new LinkedHashMap<>();
        w.put("bias", -1.75);
        w.put("loan_amount_k", 0.008);
        w.put("annual_income_k", -0.004);
        w.put("dti", 2.2);
        w.put("existing_debt_k", 0.004);
        w.put("term_years", 0.35);
        w.put("employment_years", -0.08);
        w.put("fico_inverse", 3.8);
        w.put("inq_last_6mths", 0.12);
        w.put("delinq_2yrs", 0.25);
        w.put("revol_util", 1.1);
        w.put("bankruptcies", 0.4);
        return w;
    }

    private double safe(BigDecimal v) {
        return v == null ? 0.0 : v.doubleValue();
    }

    private double avg(Integer a, Integer b) {
        if (a == null && b == null) return 0.0;
        if (a == null) return b;
        if (b == null) return a;
        return (a + b) / 2.0;
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
