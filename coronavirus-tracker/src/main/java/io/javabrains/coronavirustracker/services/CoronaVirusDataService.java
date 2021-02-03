package io.javabrains.coronavirustracker.services;

import io.javabrains.coronavirustracker.models.CountyStats;
import io.javabrains.coronavirustracker.models.StateStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.math.BigDecimal;
@Service
public class CoronaVirusDataService {
    private static String VIRUS_DATA_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private static String VIRUS_DEATH_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";
    private static String US_VIRUS_DATA_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_US.csv";
    private static String US_VIRUS_DEATH_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_US.csv";
    private List<StateStats> stateStats = new ArrayList<>();
    private TreeMap<String, ArrayList<CountyStats>> countyStats = new TreeMap<String, ArrayList<CountyStats>>();
    private int totalWorldCases=0;
    private int totalWorldDeath=0;
    private int newWorldCases=0;
    private int newWorldDeath=0;
    private double worldFatality=0;

    public int getTotalWorldCases() {
        return totalWorldCases;
    }

    public int getNewWorldCases() {
        return newWorldCases;
    }

    public int getNewWorldDeath() {
        return newWorldDeath;
    }

    public double getWorldFatality() {
        return worldFatality;
    }

    public int getTotalWorldDeath() {
        return totalWorldDeath;
    }

    public List<StateStats> getStateStats() {
        return stateStats;
    }

    public TreeMap<String, ArrayList<CountyStats>> getCountyStats() {
        return countyStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchUsVirusData() throws IOException, InterruptedException {
        List<StateStats> newStateStats = new ArrayList<>();
        List<CountyStats> tempList = new ArrayList<CountyStats>();
        TreeMap<String, ArrayList<CountyStats>> newCountyStats = new TreeMap<String, ArrayList<CountyStats>>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(US_VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse=client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);;
        for (CSVRecord record : records) {
            CountyStats countyStats = new CountyStats();
            int latestCases = Integer.parseInt(record.get(record.size()-1));
            int prevCountyCases = Integer.parseInt(record.get(record.size()-2));
            String county = record.get("Admin2");
            String state = record.get("Province_State");
            countyStats.setDiffFromPrevDay(latestCases-prevCountyCases);
            countyStats.setCumulativeCases(latestCases);
            countyStats.setCounty(county);
            countyStats.setState(state);
            tempList.add(countyStats);
            if(newCountyStats.containsKey(state)){
                ArrayList<CountyStats> newList=newCountyStats.get(state);
                newList.add(countyStats);
                newCountyStats.put(state,newList);
            }
            else{
                ArrayList<CountyStats> newList1=new ArrayList<>();
                newList1.add(countyStats);
                newCountyStats.put(state,newList1);
            }
        }
        HttpClient client1 = HttpClient.newHttpClient();
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(US_VIRUS_DEATH_URL))
                .build();
        HttpResponse<String> httpResponse1=client1.send(request1, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader1 = new StringReader(httpResponse1.body());
        Iterable<CSVRecord> records1 = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader1);
        for(CSVRecord record2: records1) {
            for(int i=0;i<tempList.size();i++){
                CountyStats c = tempList.get(i);
                if(c.getCounty().equals(record2.get("Admin2")) && c.getState().equals(record2.get("Province_State"))){
                    int latestDeathCases = Integer.parseInt(record2.get(record2.size()-1))-Integer.parseInt(record2.get(record2.size()-2));
                    int totalDeathCases = Integer.parseInt(record2.get(record2.size()-1));
                    c.setDeathDiffFromPrevDay(latestDeathCases);
                    c.setCumulativeDeath(totalDeathCases);
                    double fatality=0;
                    if(c.getCumulativeCases()!=0){
                        fatality=((double)totalDeathCases)/c.getCumulativeCases();
                    }
                    BigDecimal bd = new BigDecimal(fatality).setScale(2, RoundingMode.HALF_UP);
                    c.setFatalityRate(bd.doubleValue());
                }
            }
        }
        for(List<CountyStats> c : newCountyStats.values()){
            StateStats stateStats = new StateStats();
            int totalStateCount=0;
            int totalDeathCount=0;
            int deathDiff=0;
            int caseDiff=0;
            String state="";
            for(CountyStats stat : c) {
                state = stat.getState();
                totalStateCount += stat.getCumulativeCases();
                totalDeathCount += stat.getCumulativeDeath();
                deathDiff += stat.getDeathDiffFromPrevDay();
                caseDiff += stat.getDiffFromPrevDay();
            }
            if(!state.equals("")) {
                stateStats.setState(state);
                stateStats.setCumulativeCases(totalStateCount);
                stateStats.setCumulativeDeath(totalDeathCount);
                stateStats.setDiffFromPrevDay(caseDiff);
                stateStats.setDeathDiffFromPrevDay(deathDiff);
                double fatality = 0;
                if (stateStats.getCumulativeCases() != 0) {
                    fatality = ((double) totalDeathCount) / stateStats.getCumulativeCases();
                }
                BigDecimal bd = new BigDecimal(fatality).setScale(2, RoundingMode.HALF_UP);
                stateStats.setFatalityRate(bd.doubleValue());
                newStateStats.add(stateStats);
            }
        }
        this.stateStats=newStateStats;
        this.countyStats=newCountyStats;
    }
    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        int tmpTotalWorldCases=0;
        int tmpTotalWorldDeath=0;
        int tmpNewWorldCases=0;
        int tmpNewWorldDeath=0;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse=client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            tmpTotalWorldCases+=Integer.parseInt(record.get(record.size()-1));
            tmpNewWorldCases+=Integer.parseInt(record.get(record.size()-1))-Integer.parseInt(record.get(record.size()-2));
        }
        HttpClient client1 = HttpClient.newHttpClient();
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DEATH_URL))
                .build();
        HttpResponse<String> httpResponse1=client1.send(request1, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader1 = new StringReader(httpResponse1.body());
        Iterable<CSVRecord> records1 = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader1);
        for (CSVRecord record : records1) {
            tmpTotalWorldDeath+=Integer.parseInt(record.get(record.size()-1));
            tmpNewWorldDeath+=Integer.parseInt(record.get(record.size()-1))-Integer.parseInt(record.get(record.size()-2));
        }
        this.newWorldDeath=tmpNewWorldDeath;
        this.newWorldCases=tmpNewWorldCases;
        this.totalWorldCases=tmpTotalWorldCases;
        this.totalWorldDeath=tmpTotalWorldDeath;
        this.worldFatality=((double)tmpNewWorldDeath)/tmpTotalWorldDeath;
    }

}
