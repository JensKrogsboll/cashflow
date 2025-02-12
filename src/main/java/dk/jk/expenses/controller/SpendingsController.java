package dk.jk.expenses.controller;

import dk.jk.expenses.service.SpendingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/spendings")
@RequiredArgsConstructor
public class SpendingsController {

    private final SpendingsService spendingsService;

    @GetMapping
    public Map<String, BigDecimal> getSpendings(@RequestParam("start") String start,
                                                @RequestParam("end") String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return spendingsService.getMonthlySpendings(startDate, endDate);
    }
}
