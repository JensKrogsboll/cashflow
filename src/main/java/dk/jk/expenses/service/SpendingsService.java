package dk.jk.expenses.service;

import dk.jk.expenses.entity.Label;
import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.repository.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

        var result = postings.stream()
                .collect(
                        Collectors.groupingBy(p ->
                                        p.getEffectiveLabel().getName(),
                                TreeMap::new,
                                Collectors.groupingBy(
                                        p -> YearMonth.from(p.getDate()),
                                        TreeMap::new, // inner: TreeMap<Month, BigDecimal>
                                        Collectors.reducing(BigDecimal.ZERO, Posting::getAmount, BigDecimal::add)
                                )
                        )
                );

        // Step 2: Collect all unique YearMonths
        Set<YearMonth> allMonths = postings.stream()
                .map(p -> YearMonth.from(p.getDate()))
                .collect(Collectors.toCollection(TreeSet::new)); // ordered set

        // Step 3: Fill in missing months
        return result.entrySet().stream()
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
                        TreeMap::new
                ));
    }
}
