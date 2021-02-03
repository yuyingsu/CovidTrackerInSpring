package io.javabrains.coronavirustracker.controllers;

import io.javabrains.coronavirustracker.models.CountyStats;
import io.javabrains.coronavirustracker.models.StateStats;
import io.javabrains.coronavirustracker.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Controller
public class HomeController {
    @Autowired
    CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/")
    public String home(Model model){
        int worldCases = coronaVirusDataService.getTotalWorldCases();
        int newWorldCases = coronaVirusDataService.getNewWorldCases();
        int worldDeath = coronaVirusDataService.getTotalWorldDeath();
        int newWorldDeath = coronaVirusDataService.getNewWorldDeath();
        List<StateStats> stateStats = coronaVirusDataService.getStateStats();
        TreeMap<String, ArrayList<CountyStats>> countyStats = coronaVirusDataService.getCountyStats();
        ArrayList<CountyStats> countyStatsList = new ArrayList<>();
        int totalUsCases = stateStats.stream().mapToInt(stat -> stat.getCumulativeCases()).sum();
        int totalUsDeath = stateStats.stream().mapToInt(stat -> stat.getCumulativeDeath()).sum();
        int newUsDeath = stateStats.stream().mapToInt(stat -> stat.getDeathDiffFromPrevDay()).sum();
        int newUsCases = stateStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        double usFatality=((double)totalUsDeath)/totalUsCases;
        BigDecimal bd = new BigDecimal(usFatality).setScale(2, RoundingMode.HALF_UP);
        for(String s:countyStats.keySet()){
            for(CountyStats l: countyStats.get(s)){
                countyStatsList.add(l);
            }
        }
        model.addAttribute("worldCases", worldCases);
        model.addAttribute("newWorldCases", newWorldCases);
        model.addAttribute("worldDeath", worldDeath);
        model.addAttribute("newWorldDeath", newWorldDeath);
        model.addAttribute("stateStats",stateStats);
        model.addAttribute("countyStats",countyStatsList);
        model.addAttribute("totalUsCases",totalUsCases);
        model.addAttribute("totalUsDeath",totalUsDeath);
        model.addAttribute("newUsCases",newUsCases);
        model.addAttribute("newUsDeath",newUsDeath);
        model.addAttribute("usFatality",bd.doubleValue());
        return "home";
    }
}
