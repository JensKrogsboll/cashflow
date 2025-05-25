package dk.jk.expenses.controller;

import dk.jk.expenses.entity.Label;
import dk.jk.expenses.service.SpendingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/spendings")
@RequiredArgsConstructor
public class SpendingsController {

    private final SpendingsService spendingsService;

    @GetMapping
    public Map<String, BigDecimal> getSpendings(@RequestParam(name = "start", required = false) LocalDate start,
                                                @RequestParam(name = "end", required = false) LocalDate end) {
        return spendingsService.getMonthlySpendings(start, end);
    }

    @GetMapping("detailed")
    public Map<String, Map<YearMonth, BigDecimal>> getSpendingsDetailed(@RequestParam(name = "start", required = false) LocalDate start,
                                                                       @RequestParam(name = "end", required = false) LocalDate end) {
        return spendingsService.getMonthlySpendingsDetailed(start, end);
    }
}
