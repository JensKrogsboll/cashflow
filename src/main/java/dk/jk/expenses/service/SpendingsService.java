package dk.jk.expenses.service;

import dk.jk.expenses.entity.Label;
import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.repository.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpendingsService {

    private final PostingRepository postingRepository;

    public Map<String, BigDecimal> getMonthlySpendings(LocalDate start, LocalDate end) {
        var fromDate = start == null ? LocalDate.MIN : start;
        var toDate = end == null ? LocalDate.MAX : end;

        List<Posting> postings = postingRepository.findAll().stream()
                .filter(p -> !p.getDate().isBefore(fromDate) && !p.getDate().isAfter(toDate))
                .toList();

        Map<String, BigDecimal> monthlyTotals = new HashMap<>();
        for (Posting p : postings) {
            String monthKey = p.getDate().toString().substring(0, 7); // e.g. "2025-01"
            monthlyTotals.put(monthKey,
                    monthlyTotals.getOrDefault(monthKey, BigDecimal.ZERO).add(p.getAmount()));
        }
        return monthlyTotals;
    }

    public Map<String, Map<YearMonth, BigDecimal>> getMonthlySpendingsDetailed(LocalDate start, LocalDate end) {
        var fromDate = start == null ? LocalDate.MIN : start;
        var toDate = end == null ? LocalDate.MAX : end;

        var postings = postingRepository.findAll().stream()
                .filter(p -> !p.getDate().isBefore(fromDate) && !p.getDate().isAfter(toDate))
                .toList();

        // 1. Group by category and then by YearMonth
        var result = postings.stream()
                .collect(
                        Collectors.groupingBy(p -> p.getEffectiveLabel().getName(),
                                TreeMap::new,
                                Collectors.groupingBy(
                                        p -> YearMonth.from(p.getDate()),
                                        TreeMap::new,
                                        Collectors.reducing(BigDecimal.ZERO, Posting::getAmount, BigDecimal::add)
                                )
                        )
                );

        // 2. Collect all unique YearMonths
        Set<YearMonth> allMonths = postings.stream()
                .map(p -> YearMonth.from(p.getDate()))
                .collect(Collectors.toCollection(TreeSet::new)); // sorted

        // 3. Fill in missing months with null
        var filledResult = result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Map<YearMonth, BigDecimal> original = entry.getValue();
                            Map<YearMonth, BigDecimal> filled = new TreeMap<>();
                            for (YearMonth ym : allMonths) {
                                filled.put(ym, original.getOrDefault(ym, null));
                            }
                            return filled;
                        },
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        // 4. Add synthetic month YearMonth.of(9999, 12) with monthly averages per category
        YearMonth syntheticMonth = YearMonth.of(9999, 12);
        for (var entry : filledResult.entrySet()) {
            var values = entry.getValue().values().stream().toList();

            BigDecimal average = values.isEmpty()
                    ? null
                    : values.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), RoundingMode.HALF_UP);

            entry.getValue().put(syntheticMonth, average);
        }

        // 5. Build "Total" and "Average" categories
        Map<YearMonth, BigDecimal> totalMap = new TreeMap<>();

        Set<YearMonth> allWithSynthetic = new TreeSet<>(allMonths);
        allWithSynthetic.add(syntheticMonth);

        for (YearMonth ym : allWithSynthetic) {
            var values = filledResult.values().stream()
                    .map(m -> m.get(ym))
                    .toList();

            BigDecimal sum = values.stream()
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalMap.put(ym, sum);
        }
        var sortedResult = filledResult.entrySet().stream()
                .sorted((o1, o2) ->
                        o1.getValue().values().stream().reduce((v1, v2) -> v2).get().compareTo(
                                o2.getValue().values().stream().reduce((v1, v2) -> v2).get()
                        ))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new)
                );

        sortedResult.put("Total", totalMap);

        return sortedResult;
    }

}
