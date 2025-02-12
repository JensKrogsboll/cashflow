package dk.jk.expenses.service;

import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.repository.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpendingsService {

    private final PostingRepository postingRepository;

    public Map<String, BigDecimal> getMonthlySpendings(LocalDate start, LocalDate end) {
        List<Posting> postings = postingRepository.findAll().stream()
                .filter(p -> !p.getDate().isBefore(start) && !p.getDate().isAfter(end))
                .collect(Collectors.toList());

        Map<String, BigDecimal> monthlyTotals = new HashMap<>();
        for (Posting p : postings) {
            String monthKey = p.getDate().toString().substring(0, 7); // e.g. "2025-01"
            monthlyTotals.put(monthKey,
                    monthlyTotals.getOrDefault(monthKey, BigDecimal.ZERO).add(p.getAmount()));
        }
        return monthlyTotals;
    }
}
